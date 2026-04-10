# PollutionGO - Open Source Air Quality Monitoring

**PollutionGO** is an open-source, end-to-end air quality monitoring platform. It combines a low-cost, portable sensor unit (ESP32-based), a mobile application, and a cloud‑ready server backend to collect, visualise, and share real-time environmental data. The project was created by the **HAW‑Hatim** team to make air quality monitoring accessible, affordable, and transparent.

## Table of Contents

- [Features](#features)
- [Repository Structure](#repository-structure)
- [Prerequisites](#prerequisites)
- [Hardware Setup](#hardware-setup)
- [ESP32 Firmware](#esp32-firmware)
- [Server Setup](#server-setup)
- [Mobile App](#mobile-app)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Real‑time air quality measurements:** PM1.0, PM2.5, PM10, CO₂, temperature, humidity, and atmospheric pressure.
- **Bluetooth Low Energy (BLE) communication** between the sensor unit and the mobile app.
- **REST API server** (FastAPI) for data upload and retrieval, including GeoJSON export for mapping.
- **Flexible data filtering** by geographic region and time range.
- **Modular hardware design** – easy to build, modify, and extend.
- **Fully open‑source** – hardware schematics, firmware, app, and server code are all available.

---

## Repository Structure

```
PollutionGO/
├── Hardware/               # 3D‑printable enclosure design and assembly guide
├── ESP32 Code/             # Arduino sketch for the ESP32 sensor unit
├── App/                    # Mobile application source code (Android/iOS)
└── Server/                 # FastAPI server backend with MariaDB integration
```

---

## Prerequisites

### Hardware
- ESP32 development board (e.g., ESP32‑WROOM‑32)
- Sensors:
  - Adafruit PM2.5 Air Quality Sensor (PM25AQI)
  - Adafruit LPS22 Pressure/Temperature Sensor
  - Sensirion SCD41 CO₂ Sensor
- Connecting wires, breadboard, power supply
- (Optional) 3D printer for the enclosure

### Software
- [Arduino IDE](https://www.arduino.cc/en/software) (for ESP32 firmware)
- [Python 3.8+](https://www.python.org/) (for the server)
- [MariaDB 11.8+](https://mariadb.org/) (database)
- [Node.js / React Native](https://reactnative.dev/) (if you plan to build the mobile app from source)

---

## Hardware Setup

The hardware consists of an ESP32 that reads data from three sensors over I²C. The wiring diagram and a 3D‑printable case are provided in the `Hardware/Case` folder.

### Sensor Connections

| ESP32 Pin | Sensor       | Function |
|-----------|--------------|----------|
| 21 (SDA)  | All sensors  | I²C Data |
| 22 (SCL)  | All sensors  | I²C Clock |
| 3.3V / 5V | VCC          | Power    |
| GND       | GND          | Ground   |

> ⚠️ **Note:** The sensors require 3.3V logic. Ensure your ESP32 is set to 3.3V output.

### Enclosure
- The `Hardware/Case` folder contains STL files for a compact, weather‑resistant enclosure.
- Print the parts with PLA/PETG and assemble using M3 screws.

---

## ESP32 Firmware

The ESP32 firmware is an Arduino sketch located in `ESP32 Code/Messgeraet.ino`. It reads the sensors, formats the data as a comma‑separated string, and broadcasts it via BLE.

### Setup Instructions

1. Install the required Arduino libraries:
   - `Adafruit PM25 AQI Sensor`
   - `Adafruit LPS22`
   - `Sensirion I2C SCD4x`
2. Open `Messgeraet.ino` in the Arduino IDE.
3. Select your ESP32 board (e.g., `ESP32 Dev Module`) and the correct COM port.
4. Upload the sketch.

### BLE Service UUID
The device advertises with the service UUID `6E400001-B5A3-F393-E0A9-E50E24DCCA9E` and sends data as a string formatted as:
```
temperature,humidity,pressure,co2,pm1_0,pm2_5,pm10
```

---

## Server Setup

The backend is a **FastAPI** server that stores sensor data in a MariaDB database and exposes a REST API. The server is designed to run on a Raspberry Pi but works on any Linux/macOS/Windows machine.

### Installation

```bash
# Clone the repository
git clone https://github.com/HAW-Hatim/PollutionGO.git
cd PollutionGO/Server

# Install Python dependencies
pip install fastapi uvicorn sqlalchemy pymysql geojson pydantic
```

### Database Configuration

1. **Create the database:**
   ```sql
   mysql -u root -p < database.sql
   ```

2. **Create a database user (if needed):**
   ```sql
   CREATE USER 'fastapiuser'@'localhost' IDENTIFIED BY 'Air1234';
   GRANT ALL PRIVILEGES ON PollutionDB.* TO 'fastapiuser'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Verify the connection string** in `main.py`:
   ```python
   DATABASE_URL = "mysql+pymysql://fastapiuser:Air1234@localhost/PollutionDB"
   ```

### Running the Server

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

The API will be available at `http://localhost:8000`. Interactive documentation is automatically generated at `/docs`.

### API Endpoints

| Method | Endpoint     | Description |
|--------|--------------|-------------|
| GET    | `/`          | Health check |
| POST   | `/upload`    | Submit a new measurement |
| GET    | `/download`  | Retrieve measurements (supports geographic and time filters) |

For detailed API usage, refer to the [Server README](Server/README.md).

---

## Mobile App

The mobile app (Android/iOS) connects to the ESP32 via BLE, receives real‑time sensor data, and uploads it to the server. The app also displays historical data on a map.

### Building from Source

> The app source code is located in the `App` folder. Detailed build instructions are being prepared and will be added soon. In the meantime, you can import the project into Android Studio or Xcode and build it using the respective toolchains.

### Pre‑built APK / IPA

Pre‑built binaries will be made available in the [Releases](https://github.com/HAW-Hatim/PollutionGO/releases) section.

---

## Usage

1. **Power on the ESP32 sensor unit.** The onboard LED will indicate BLE advertising.
2. **Open the mobile app** and scan for nearby devices. Connect to `ESP32_BLE_UART`.
3. **Collect data** – the app will display live readings and automatically upload them to the configured server.
4. **View historical data** on the map or export it as GeoJSON for further analysis.

---

## Contributing

We welcome contributions! Here’s how you can help:

- **Report bugs** by opening an [issue](https://github.com/HAW-Hatim/PollutionGO/issues).
- **Suggest new features** or improvements.
- **Submit pull requests** for code, documentation, or hardware designs.

Please read our [Contributing Guidelines](CONTRIBUTING.md) (to be added) for more details.

---

## License

This project is licensed under the **MIT License** – see the [LICENSE](LICENSE) file for details.

