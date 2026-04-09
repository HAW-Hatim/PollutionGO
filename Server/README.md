# PollutionGO Server

Ein FastAPI-basierter Server zur Erfassung und Verwaltung von Luftqualitätsmessungen und Umweltdaten mit geografischen Koordinaten.

## 📋 Features

- **REST API** für Datenupload und -download
- **GeoJSON-Export** für Kartendarstellung
- **Flexible Filter** nach geografischer Region und Zeitraum
- **MariaDB Datenbank** auf Raspberry Pi
- **Echtzeit-Datenerfassung** von Sensoren

## 🛠️ Anforderungen

- Python 3.8 oder höher
- MariaDB 11.8 oder höher
- pip (Python Package Manager)

### Python Dependencies
- fastapi
- uvicorn
- sqlalchemy
- pymysql
- geojson
- pydantic

## 📦 Installation

### 1. Repository klonen
```bash
git clone https://github.com/MP-U-Code/PollutionGO-server.git
cd PollutionGO-server
```

### 2. Python Dependencies installieren
```bash
pip install fastapi uvicorn sqlalchemy pymysql geojson pydantic
```

### 3. MariaDB Datenbank erstellen
```bash
mysql -u root -p < database.sql
```

### 4. Datenbank-Benutzer anlegen (falls nicht vorhanden)
```bash
mysql -u root -p
```

```sql
CREATE USER 'fastapiuser'@'localhost' IDENTIFIED BY 'Air1234';
GRANT ALL PRIVILEGES ON PollutionDB.* TO 'fastapiuser'@'localhost';
FLUSH PRIVILEGES;
```

### 5. Datenbankverbindung überprüfen
In `main.py` prüfen, dass die Verbindungsdaten korrekt sind:
```python
DATABASE_URL = "mysql+pymysql://fastapiuser:Air1234@localhost/PollutionDB"
```

## 🚀 Server starten

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

Der Server läuft dann unter: `http://localhost:8000`

## 📡 API Endpoints

### 1. Health Check
**GET** `/`
```
Response: {"message": "Hello World from Raspberry Pi!"}
```

### 2. Messdaten hochladen
**POST** `/upload`

Request Body:
```json
{
  "datum": "2026-04-05",
  "zeit": "14:30:00",
  "breitengrad": 51.5074,
  "laengengrad": -0.1278,
  "pm1_0": 5.2,
  "pm2_5": 10.5,
  "pm10": 25.3,
  "co2": 400.0,
  "temperatur": 22.5,
  "luftdruck": 1013.25,
  "luftfeuchtigkeit": 65.0
}
```

Response:
```json
{"status": "ok"}
```

### 3. Daten abrufen (mit Filtern)
**GET** `/download`

Query Parameter (optional):
- `lat_min`, `lat_max` - Breitengrad-Bereich
- `lon_min`, `lon_max` - Längengrad-Bereich
- `start_date` - Startdatum (YYYY-MM-DD)
- `start_time` - Startzeit (HH:MM:SS)
- `end_date` - Enddatum (YYYY-MM-DD)
- `end_time` - Endzeit (HH:MM:SS)

Beispiel:
```
GET /download?lat_min=51.5&lat_max=51.6&lon_min=-0.2&lon_max=-0.1&start_date=2026-04-01&end_date=2026-04-05
```

Response: GeoJSON FeatureCollection mit allen gefilterten Messwerten

## 📊 Datenbank Schema

### Tabelle: Messwerte
```
- id (INT, Primary Key, Auto-Increment)
- datum (DATE)
- zeit (TIME)
- breitengrad (FLOAT)
- laengengrad (FLOAT)
- pm1_0 (FLOAT) - Feinstaubpartikel 1.0 µm
- pm2_5 (FLOAT) - Feinstaubpartikel 2.5 µm
- pm10 (FLOAT) - Feinstaubpartikel 10 µm
- co2 (FLOAT) - Kohlendioxid in ppm
- temperatur (FLOAT) - in °C
- luftdruck (FLOAT) - in hPa
- luftfeuchtigkeit (FLOAT) - in %
```

## 🔧 Beispiel: Daten mit curl hochladen

```bash
curl -X POST "http://localhost:8000/upload" \
  -H "Content-Type: application/json" \
  -d '{
    "datum": "2026-04-05",
    "zeit": "14:30:00",
    "breitengrad": 51.5074,
    "laengengrad": -0.1278,
    "pm1_0": 5.2,
    "pm2_5": 10.5,
    "pm10": 25.3,
    "co2": 400.0,
    "temperatur": 22.5,
    "luftdruck": 1013.25,
    "luftfeuchtigkeit": 65.0
  }'
```

## 📝 Notizen

- Die Datenbank läuft auf einem Raspberry Pi
- Alle Zeitstempel sind im UTC-Format (GMT+00:00)
- GeoJSON-Format: Längengrad vor Breitengrad (Standard GeoJSON)

## 📄 Lizenz

MIT License

## 👤 Autor

MP-U-Code

---

**Letztes Update:** 2026-04-05