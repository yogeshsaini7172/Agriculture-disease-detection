import os
from datetime import datetime
from dotenv import load_dotenv

load_dotenv()

MONGO_URI     = os.getenv("MONGO_URI", "")
MONGO_DB_NAME = os.getenv("MONGO_DB_NAME", "agrotechDatabase")

# ── Graceful init: don't crash if MONGO_URI is missing/placeholder ────────────
_PLACEHOLDER_URIS = {"", "mongodb+srv://<username>:<password>@<cluster>.mongodb.net/<dbname>"}

db = None
users_col           = None
reports_col         = None
stress_analysis_col = None
devices_col         = None   # 🆕 Device → Farmer mapping
iot_data_col        = None   # 🆕 Per-farmer time-series sensor data

try:
    if MONGO_URI and MONGO_URI not in _PLACEHOLDER_URIS:
        from pymongo import MongoClient
        client  = MongoClient(MONGO_URI, serverSelectionTimeoutMS=5000)
        db      = client.get_database(MONGO_DB_NAME)
        users_col           = db.get_collection("users")
        reports_col         = db.get_collection("reports")
        stress_analysis_col = db.get_collection("stress_analysis")
        devices_col         = db.get_collection("devices")
        iot_data_col        = db.get_collection("iot_sensor_data")

        # ── Indexes for performance ──────────────────────────────────────────
        users_col.create_index("mobile_number", unique=True, sparse=True)
        devices_col.create_index("device_id", unique=True, sparse=True)
        devices_col.create_index("user_id")
        iot_data_col.create_index([("user_id", 1), ("timestamp", -1)])

        print("INFO  [MongoDB]: Connected to Atlas successfully.")
    else:
        print("WARN  [MongoDB]: MONGO_URI not set - running without database (demo mode).")
except Exception as e:
    print(f"WARN  [MongoDB]: Connection failed ({e}) - running without database.")


# ─────────────────────────────────────────────────────────────────────────────
# USER HELPERS
# ─────────────────────────────────────────────────────────────────────────────

def get_user_by_email(email):
    """Legacy helper kept for backward-compat (maps to mobile query)."""
    if users_col is None:
        return None
    try:
        return users_col.find_one({"email": email})
    except Exception:
        return None


def get_user_by_mobile(mobile_number: str):
    """Fetch a user document by mobile number."""
    if users_col is None:
        return None
    try:
        return users_col.find_one({"mobile_number": mobile_number})
    except Exception:
        return None


def get_user_by_id(user_id: str):
    """Fetch a user document by its string _id."""
    if users_col is None:
        return None
    try:
        from bson import ObjectId
        return users_col.find_one({"_id": ObjectId(user_id)})
    except Exception:
        return None


def create_user(user_data: dict):
    """Insert a new user document and return the InsertOneResult."""
    if users_col is None:
        return type("FakeResult", (), {"inserted_id": "demo_id"})()
    try:
        return users_col.insert_one(user_data)
    except Exception:
        return type("FakeResult", (), {"inserted_id": "demo_id"})()


def list_all_users():
    """Return all user documents (admin use)."""
    if users_col is None:
        return []
    try:
        return list(users_col.find({}, {"password": 0}))  # exclude passwords
    except Exception:
        return []


# ─────────────────────────────────────────────────────────────────────────────
# DEVICE MAPPING HELPERS
# ─────────────────────────────────────────────────────────────────────────────

def get_device(device_id: str):
    """Fetch a device document by device_id."""
    if devices_col is None:
        return None
    try:
        return devices_col.find_one({"device_id": device_id})
    except Exception:
        return None


def get_device_by_user(user_id: str):
    """Return the device mapped to a specific user_id (if any)."""
    if devices_col is None:
        return None
    try:
        return devices_col.find_one({"user_id": user_id})
    except Exception:
        return None


def map_device_to_user(device_id: str, user_id: str, farmer_name: str = ""):
    """
    Map device_id to user_id.
    - If the device doesn't exist yet, raise ValueError (it must be discovered by the backend first).
    - Raises ValueError if the device is already mapped to a DIFFERENT user.
    """
    if devices_col is None:
        # Demo mode: silently succeed
        return True

    existing = get_device(device_id)
    if not existing:
        raise ValueError(f"Sensor '{device_id}' does not exist. Ensure your hardware is turned on and connected to the server.")

    if existing.get("user_id"):
        if existing["user_id"] == user_id:
            return True   # Already mapped to the same user, no-op
        raise ValueError(f"Device '{device_id}' is already mapped to another farmer.")

    # Update existing unmapped device
    devices_col.update_one(
        {"device_id": device_id},
        {"$set": {
            "user_id": user_id,
            "farmer_name": farmer_name,
            "connected_at": datetime.utcnow(),
            "active": True
        }}
    )
    return True


def unmap_device(device_id: str, user_id: str):
    """Remove the mapping for a device (only allowed by the owning user)."""
    if devices_col is None:
        return False
    try:
        result = devices_col.delete_one({"device_id": device_id, "user_id": user_id})
        return result.deleted_count > 0
    except Exception:
        return False


def list_all_devices():
    """Return all device mappings (admin use)."""
    if devices_col is None:
        return []
    try:
        return list(devices_col.find({}))
    except Exception:
        return []


# ─────────────────────────────────────────────────────────────────────────────
# IOT SENSOR DATA HELPERS (per-user)
# ─────────────────────────────────────────────────────────────────────────────

def save_iot_reading(user_id: str, device_id: str, reading: dict):
    """Persist a single IoT sensor reading, tagged with user_id and device_id."""
    if iot_data_col is None:
        return None
    try:
        doc = {
            "user_id":   user_id,
            "device_id": device_id,
            "timestamp": datetime.utcnow(),
            **reading,
        }
        return iot_data_col.insert_one(doc)
    except Exception as e:
        print(f"WARN  [MongoDB] save_iot_reading failed: {e}")
        return None


def get_iot_history_for_user(user_id: str, limit: int = 50):
    """Return the last `limit` IoT readings for a specific user."""
    if iot_data_col is None:
        return []
    try:
        return list(
            iot_data_col
            .find({"user_id": user_id}, {"_id": 0})
            .sort("timestamp", -1)
            .limit(limit)
        )
    except Exception:
        return []


# ─────────────────────────────────────────────────────────────────────────────
# REPORT / ANALYSIS HELPERS (unchanged)
# ─────────────────────────────────────────────────────────────────────────────

def save_report(report_data: dict):
    if reports_col is None:
        return None
    try:
        return reports_col.insert_one(report_data)
    except Exception:
        return None


def save_stress_analysis(analysis_data: dict):
    if stress_analysis_col is None:
        return None
    try:
        return stress_analysis_col.insert_one(analysis_data)
    except Exception:
        return None
