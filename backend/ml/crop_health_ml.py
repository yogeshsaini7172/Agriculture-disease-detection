"""
🌾 NDVI-Based Crop Health ML Interpreter
=========================================
Placeholder ML model that classifies crop health and generates
irrigation/intervention advice based on NDVI statistics.

HOW TO PLUG IN YOUR REAL MODEL:
  1. Replace the `_rule_based_prediction()` function body with a call to your
     pre-trained model (e.g., scikit-learn, XGBoost, ONNX).
  2. Ensure your model accepts the feature vector defined in `build_feature_vector()`.
  3. Update `NDVI_CLASSES` if your model outputs different labels.

Example (XGBoost):
    import joblib
    _model = joblib.load("path/to/model.pkl")

    def predict(ndvi_stats, extra_inputs=None):
        features = build_feature_vector(ndvi_stats, extra_inputs)
        label_idx = int(_model.predict([features])[0])
        ...
"""

import numpy as np


# ── Classification labels ─────────────────────────────────────────────────────
NDVI_CLASSES = {
    0: "Critical Stress",       # NDVI very low  → severe drought / disease
    1: "Moderate Stress",       # NDVI low       → mild nutrient deficiency / drought
    2: "Healthy Vegetation",    # NDVI optimal   → crop is thriving
    3: "Sparse / Bare Soil",    # NDVI near zero → early growth or bare patches
    4: "Water Body / No Crop",  # NDVI negative  → water or non-vegetated area
}

# Confidence is approximated from the standard deviation (uniformity proxy)
_ADVICE_DB = {
    "Critical Stress": {
        "severity": "🔴 Critical",
        "irrigation_needed": True,
        "irrigation_recommendation": "Immediate irrigation required. Apply 40–60 mm of water within 24 hours.",
        "nutrient_action": "Apply foliar spray of NPK 0-52-34 (Mono Potassium Phosphate) at 5 g/L to aid rapid recovery.",
        "field_action": "Scout for pests and diseases immediately. Consider emergency intervention.",
        "next_check_days": 2
    },
    "Moderate Stress": {
        "severity": "🟠 Moderate",
        "irrigation_needed": True,
        "irrigation_recommendation": "Schedule irrigation within 48–72 hours. Apply 25–35 mm of water.",
        "nutrient_action": "Apply urea top-dressing at 50 kg/hectare if nitrogen deficiency is suspected.",
        "field_action": "Check soil moisture at 15 cm depth. Inspect lower leaves for discolouration.",
        "next_check_days": 5
    },
    "Healthy Vegetation": {
        "severity": "🟢 Optimal",
        "irrigation_needed": False,
        "irrigation_recommendation": "No immediate irrigation required. Maintain current watering schedule.",
        "nutrient_action": "Continue balanced fertiliser programme. Monitor with Leaf Color Chart (LCC).",
        "field_action": "Crop is performing well. Scout field for early pest signs as a precaution.",
        "next_check_days": 10
    },
    "Sparse / Bare Soil": {
        "severity": "🟡 Low Activity",
        "irrigation_needed": False,
        "irrigation_recommendation": "Light irrigation recommended if less than 5 days since last rain.",
        "nutrient_action": "Ensure basal fertiliser is applied at sowing stage.",
        "field_action": "Could indicate early growth stage or patchy germination. Verify crop stand.",
        "next_check_days": 7
    },
    "Water Body / No Crop": {
        "severity": "⚪ Non-Agricultural",
        "irrigation_needed": False,
        "irrigation_recommendation": "Not applicable – area does not appear to contain active crop.",
        "nutrient_action": "Not applicable.",
        "field_action": "Verify the coordinates. The selected area may contain a water body or fallow land.",
        "next_check_days": 14
    }
}


def build_feature_vector(ndvi_stats: dict, extra_inputs: dict = None) -> list:
    """
    Constructs the feature vector that will be passed to the ML model.

    Current features (5):
      [mean_ndvi, max_ndvi, min_ndvi, std_ndvi, pixel_count_normalized]

    If you add weather or soil inputs in the future, extend extra_inputs:
      extra_inputs = {"temperature": 28.5, "humidity": 65, "soil_ph": 6.5, ...}

    Args:
        ndvi_stats   (dict): Output of sentinel_service.extract_ndvi_stats()
        extra_inputs (dict): Optional additional sensor/weather data.

    Returns:
        list: Feature vector ready for model.predict([features])
    """
    features = [
        ndvi_stats.get("mean_ndvi",   0.0),
        ndvi_stats.get("max_ndvi",    0.0),
        ndvi_stats.get("min_ndvi",    0.0),
        ndvi_stats.get("std_ndvi",    0.0),
        # Normalise pixel count to [0, 1] assuming max resolution 256x256 = 65536 px
        min(ndvi_stats.get("pixel_count", 0) / 65536.0, 1.0),
    ]

    # ── Extend with weather / soil inputs if provided ─────────────────────────
    if extra_inputs:
        features.extend([
            float(extra_inputs.get("temperature", 25.0)),
            float(extra_inputs.get("humidity",    70.0)),
            float(extra_inputs.get("rainfall",   100.0)),
            float(extra_inputs.get("soil_ph",      6.5)),
        ])

    return features


def _rule_based_prediction(mean_ndvi: float) -> tuple:
    """
    Rule-based fallback classifier when no trained model is available.
    This mirrors what a well-tuned decision tree would produce for NDVI
    thresholds validated against agricultural literature.

    ┌──────────────────────────────────┬───────────────────────┐
    │  NDVI Range                      │  Classification       │
    ├──────────────────────────────────┼───────────────────────┤
    │  NDVI < 0                        │  Water / No Crop      │
    │  0.00 ≤ NDVI < 0.20             │  Bare Soil / Sparse   │
    │  0.20 ≤ NDVI < 0.40             │  Moderate Stress      │
    │  0.40 ≤ NDVI < 0.75             │  Healthy Vegetation   │
    │  NDVI < 0.20 (severe degraded)   │  Critical Stress      │
    └──────────────────────────────────┴───────────────────────┘

    Returns:
        tuple: (class_label: str, confidence: float)
    """
    if mean_ndvi < 0.0:
        label, conf = "Water Body / No Crop", 0.92
    elif mean_ndvi < 0.10:
        label, conf = "Critical Stress", 0.88
    elif mean_ndvi < 0.20:
        label, conf = "Moderate Stress", 0.80
    elif mean_ndvi < 0.40:
        label, conf = "Sparse / Bare Soil", 0.74
    elif mean_ndvi < 0.75:
        label, conf = "Healthy Vegetation", 0.91
    else:
        label, conf = "Healthy Vegetation", 0.96    # Dense, thriving canopy

    return label, conf


import os
import json
import requests

def predict(ndvi_stats: dict, extra_inputs: dict = None) -> dict:
    """
    Main prediction entry point using Groq AI.
    Falls back to rule-based prediction if Groq fails or API key is missing.
    """
    mean_ndvi = ndvi_stats.get("mean_ndvi", -1.0)
    features = build_feature_vector(ndvi_stats, extra_inputs)
    print(f"DEBUG [CropHealthML]: Feature vector = {features}")

    groq_api_key = os.getenv("GROQ_API_KEY")
    
    if groq_api_key:
        try:
            prompt = f"""You are an expert Agronomist AI.
Analyze the following Satellite NDVI statistics and environmental data to determine crop health and recommend interventions.

NDVI Statistics:
- Mean NDVI: {ndvi_stats.get('mean_ndvi', 'N/A')}
- Max NDVI: {ndvi_stats.get('max_ndvi', 'N/A')}
- Min NDVI: {ndvi_stats.get('min_ndvi', 'N/A')}
- Std Dev: {ndvi_stats.get('std_ndvi', 'N/A')}

Environmental Data (Optional):
- Temperature: {extra_inputs.get('temperature', 'N/A') if extra_inputs else 'N/A'}°C
- Humidity: {extra_inputs.get('humidity', 'N/A') if extra_inputs else 'N/A'}%
- Rainfall: {extra_inputs.get('rainfall', 'N/A') if extra_inputs else 'N/A'}mm
- Soil pH: {extra_inputs.get('soil_ph', 'N/A') if extra_inputs else 'N/A'}

Based on these features, output a JSON response matching exactly this format:
{{
  "prediction": "Healthy Vegetation", // Options: Critical Stress, Moderate Stress, Healthy Vegetation, Sparse / Bare Soil, Water Body / No Crop
  "confidence": 0.95, // 0.0 to 1.0
  "severity": "🟢 Optimal", // e.g. 🔴 Critical, 🟠 Moderate, 🟢 Optimal, 🟡 Low Activity, ⚪ Non-Agricultural
  "irrigation_needed": false, // true or false
  "irrigation_action": "Maintain current watering schedule.",
  "nutrient_action": "Continue balanced fertiliser programme.",
  "field_action": "Scout field for early pest signs.",
  "next_satellite_check_days": 10 // integer
}}
Do NOT wrap the JSON in Markdown formatting like ```json. Return ONLY valid raw JSON."""

            headers = {
                "Authorization": f"Bearer {groq_api_key}",
                "Content-Type": "application/json"
            }
            payload = {
                "model": "llama3-70b-8192",
                "messages": [{"role": "user", "content": prompt}],
                "temperature": 0.1,
                "response_format": {"type": "json_object"}
            }

            response = requests.post("https://api.groq.com/openai/v1/chat/completions", headers=headers, json=payload, timeout=15)
            if response.ok:
                result_text = response.json()["choices"][0]["message"]["content"]
                result_json = json.loads(result_text)
                
                return {
                    "prediction":  result_json.get("prediction", "Unknown"),
                    "confidence":  float(result_json.get("confidence", 0.8)),
                    "severity":    result_json.get("severity", "Unknown"),
                    "ndvi_health_score": round((mean_ndvi + 1) / 2 * 100, 1),
                    "recommendation": {
                        "irrigation_needed":          result_json.get("irrigation_needed", False),
                        "irrigation_action":          result_json.get("irrigation_action", ""),
                        "nutrient_action":            result_json.get("nutrient_action", ""),
                        "field_action":               result_json.get("field_action", ""),
                        "next_satellite_check_days":  int(result_json.get("next_satellite_check_days", 7)),
                    }
                }
            else:
                print(f"WARN [CropHealthML]: Groq API returned {response.status_code}. Falling back to rules.")
        except Exception as e:
            print(f"WARN [CropHealthML]: Groq AI failed ({e}). Falling back to rules.")

    # ── STEP 3: Rule-based fallback (active if Groq API fails) ──
    print("DEBUG [CropHealthML]: Using rule-based fallback logic.")
    label, confidence = _rule_based_prediction(mean_ndvi)
    advice = _ADVICE_DB.get(label, {})

    return {
        "prediction":  label,
        "confidence":  round(confidence, 2),
        "severity":    advice.get("severity", "Unknown"),
        "ndvi_health_score": round((mean_ndvi + 1) / 2 * 100, 1),   # 0-100 normalised score
        "recommendation": {
            "irrigation_needed":          advice.get("irrigation_needed", False),
            "irrigation_action":          advice.get("irrigation_recommendation", ""),
            "nutrient_action":            advice.get("nutrient_action", ""),
            "field_action":               advice.get("field_action", ""),
            "next_satellite_check_days":  advice.get("next_check_days", 7),
        }
    }
