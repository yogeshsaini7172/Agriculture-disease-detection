"""
sensorlaptop.py  —  AgroTech IoT Sensor Bridge
================================================
This script runs on the LOCAL LAPTOP connected to the Arduino via USB serial.
It reads sensor values (pH, soil moisture, temperature, humidity) and POSTs
them to the AgroTech backend along with a HARDCODED DEVICE ID so the server
can route the data exclusively to the correct farmer's dashboard.

HOW TO USE
----------
1. Set DEVICE_ID below to the unique ID you registered in the AgroTech app.
2. Set SERVER_URL to the IP/hostname of your AgroTech Flask server.
3. Connect your Arduino and set ARDUINO_PORT to the correct COM port (Windows)
   or /dev/ttyUSB0 (Linux/Mac).
4. Run:  python sensorlaptop.py

ARDUINO SKETCH EXPECTED OUTPUT (Serial, 9600 baud)
---------------------------------------------------
soil:<value>,temp:<value>,humidity:<value>,ph:<value>
Example:  soil:42.5,temp:28.3,humidity:65.0,ph:6.8

DEMO / SIMULATION MODE
-----------------------
If no Arduino is connected (or USE_SIMULATION=True), the script generates
realistic synthetic sensor readings so you can test the full stack.
"""

import serial
import time
import requests
import json
import random
import math
from datetime import datetime

# ─────────────────────────────────────────────────────────────────────────────
# 🔧 CONFIGURATION  (Edit these values for your setup)
# ─────────────────────────────────────────────────────────────────────────────

# ⚠️  IMPORTANT: Set this to the Device ID you registered in the AgroTech app.
DEVICE_ID = "FARM_DEVICE_001"

# AgroTech Flask server address (use your LAN IP or Render URL)
# Since the backend is running here, change this to your laptop's public/LAN IP
SERVER_URL = "http://10.88.41.1:5000/api/iot/data"

# Arduino serial port
#   Windows:  "COM3", "COM4", etc.
#   Linux/Mac: "/dev/ttyUSB0", "/dev/ttyACM0"
ARDUINO_PORT = "COM3"
BAUD_RATE    = 9600

# Set True to run without an Arduino (generates simulated data)
USE_SIMULATION = True

# How often to send data (seconds)
SEND_INTERVAL = 5

# ─────────────────────────────────────────────────────────────────────────────
# SIMULATION ENGINE  –  Generates realistic farming sensor data
# ─────────────────────────────────────────────────────────────────────────────

_sim_tick = 0

def read_simulated_sensors() -> dict:
    """
    Returns a plausible sensor snapshot.
    Values oscillate naturally over time to mimic a real field.
    """
    global _sim_tick
    _sim_tick += 1

    hour = datetime.now().hour
    # Soil moisture drops during day, rises at night / after irrigation
    base_soil = 55 + 20 * math.sin(_sim_tick * 0.08) - (5 if 8 <= hour <= 18 else 0)
    soil_moisture = round(max(10, min(95, base_soil + random.uniform(-3, 3))), 1)

    # Temperature: warmer during day
    base_temp = 25 + 8 * math.sin((_sim_tick * 0.05) - 1.5) + (3 if 10 <= hour <= 16 else 0)
    temperature = round(max(10, min(45, base_temp + random.uniform(-0.5, 0.5))), 1)

    humidity = round(max(30, min(95, 65 + 10 * math.sin(_sim_tick * 0.06) + random.uniform(-2, 2))), 1)
    ph       = round(max(4.0, min(9.0, 6.8 + 0.3 * math.sin(_sim_tick * 0.03) + random.uniform(-0.05, 0.05))), 2)

    return {
        "soil_moisture": soil_moisture,
        "temperature":   temperature,
        "humidity":      humidity,
        "ph":            ph,
    }


# ─────────────────────────────────────────────────────────────────────────────
# ARDUINO READER
# ─────────────────────────────────────────────────────────────────────────────

def parse_arduino_line(line: str) -> dict | None:
    """
    Parse a comma-separated sensor line from Arduino.
    Expected format:  soil:42.5,temp:28.3,humidity:65.0,ph:6.8
    Returns a dict or None if parsing fails.
    """
    try:
        parts = line.strip().split(",")
        result = {}
        for part in parts:
            if ":" in part:
                key, value = part.split(":", 1)
                result[key.strip().lower()] = float(value.strip())

        # Normalise key names
        normalised = {
            "soil_moisture": result.get("soil") or result.get("soil_moisture") or result.get("moisture"),
            "temperature":   result.get("temp") or result.get("temperature"),
            "humidity":      result.get("humidity"),
            "ph":            result.get("ph"),
        }
        # Only return if we have at least soil or temp
        if normalised["soil_moisture"] is not None or normalised["temperature"] is not None:
            return {k: v for k, v in normalised.items() if v is not None}
    except Exception as e:
        print(f"  [PARSE ERROR] Could not parse line '{line}': {e}")
    return None


def read_arduino_sensors(ser: serial.Serial) -> dict | None:
    """Read one line from the serial port and parse it."""
    try:
        if ser.in_waiting > 0:
            raw = ser.readline().decode("utf-8", errors="replace")
            return parse_arduino_line(raw)
    except serial.SerialException as e:
        print(f"  [SERIAL ERROR] {e}")
    return None


# ─────────────────────────────────────────────────────────────────────────────
# HTTP UPLOADER
# ─────────────────────────────────────────────────────────────────────────────

def send_to_server(sensor_data: dict) -> bool:
    """
    POST sensor data + DEVICE_ID to the AgroTech server.
    Returns True on success, False on failure.
    """
    payload = {
        "device_id": DEVICE_ID,          # ← Hardcoded device identifier
        **sensor_data,                    # ← Sensor readings
    }

    try:
        response = requests.post(
            SERVER_URL,
            json=payload,
            timeout=10,
            headers={"Content-Type": "application/json"},
        )
        if response.ok:
            result = response.json()
            routed_to = result.get("routed_to", "unknown")
            decision  = result.get("decision", "—")
            print(
                f"  ✅ Sent  | device={DEVICE_ID} | routed_to={routed_to} "
                f"| decision={decision} | soil={sensor_data.get('soil_moisture')}% "
                f"| temp={sensor_data.get('temperature')}°C"
            )
            return True
        else:
            print(f"  ❌ Server error {response.status_code}: {response.text[:120]}")
    except requests.exceptions.ConnectionError:
        print(f"  ⚠️  Cannot reach server at {SERVER_URL}. Is the backend running?")
    except requests.exceptions.Timeout:
        print(f"  ⚠️  Request timed out. Server may be overloaded.")
    except Exception as e:
        print(f"  ❌ Unexpected error: {e}")
    return False


# ─────────────────────────────────────────────────────────────────────────────
# MAIN LOOP
# ─────────────────────────────────────────────────────────────────────────────

def main():
    print("=" * 60)
    print("  🌾 AgroTech IoT Sensor Bridge")
    print("=" * 60)
    print(f"  Device ID  : {DEVICE_ID}")
    print(f"  Server     : {SERVER_URL}")
    print(f"  Mode       : {'SIMULATION' if USE_SIMULATION else f'Arduino ({ARDUINO_PORT})'}")
    print(f"  Interval   : Every {SEND_INTERVAL} seconds")
    print("=" * 60)
    print()

    ser = None
    if not USE_SIMULATION:
        try:
            ser = serial.Serial(ARDUINO_PORT, BAUD_RATE, timeout=2)
            print(f"  📡 Arduino connected on {ARDUINO_PORT} at {BAUD_RATE} baud")
            time.sleep(2)   # Wait for Arduino reset
        except serial.SerialException as e:
            print(f"  ❌ Could not open serial port {ARDUINO_PORT}: {e}")
            print("  💡 Tip: Switch to simulation mode by setting USE_SIMULATION = True")
            return

    print("  🚀 Starting data loop... (Press Ctrl+C to stop)\n")

    try:
        while True:
            ts = datetime.now().strftime("%H:%M:%S")
            print(f"[{ts}]", end=" ")

            if USE_SIMULATION:
                sensor_data = read_simulated_sensors()
            else:
                sensor_data = read_arduino_sensors(ser)
                if sensor_data is None:
                    print("  ⏳ No data from Arduino yet, retrying...")
                    time.sleep(1)
                    continue

            send_to_server(sensor_data)
            time.sleep(SEND_INTERVAL)

    except KeyboardInterrupt:
        print("\n\n  🛑 Stopped by user. Goodbye!")
    finally:
        if ser and ser.is_open:
            ser.close()
            print("  📡 Serial port closed.")


if __name__ == "__main__":
    main()
