#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <Wire.h>               //I2C
#include <Adafruit_PM25AQI.h>   //Pollution
#include <Adafruit_LPS2X.h>     //Pressure
#include <Adafruit_Sensor.h>    //=||=
#include <SensirionI2cScd4x.h>  //CO2
 
//default is SDA = 21, SCL = 22
#define SDA 21  //choose fitting port later!
#define SCL 22  //choose fitting port later!
 
//measurement hardware (HW)
Adafruit_PM25AQI PM = Adafruit_PM25AQI();
Adafruit_LPS22 BAR;
SensirionI2cScd4x CARB;  //carbondioxid
 
//Values                //Units
uint16_t pullution = 0;  //  ppm
uint16_t co2 = 0;  //    %
float temp2 = 0;   //   °C
float humid = 0;   //    %
sensors_event_t temp1;
sensors_event_t pressure;
 
uint16_t pm1 = 0;
uint16_t pm2_5 = 0;
uint16_t pm10 = 0;
 
//other variables
bool co2_ready = false;
 
 
BLEServer *pServer = NULL;
BLECharacteristic *pTxCharacteristic;
bool deviceConnected = false;
bool advertisingRestarted = false;
 
#define SERVICE_UUID           "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_RX "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
#define CHARACTERISTIC_UUID_TX "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
 
class MyServerCallbacks : public BLEServerCallbacks {
 
  void onConnect(BLEServer *pServer) {
    deviceConnected = true;
    advertisingRestarted = false;
    delay(500);
    Serial.println("Device connected");
  }
 
  void onDisconnect(BLEServer *pServer) {
    deviceConnected = false;
    Serial.println("Device disconnected - restarting advertising...");
    //pServer->stopAdvertising();
 
    // CRITICAL: Restart advertising after a short delay
    delay(500);  // Give time for the disconnect to fully process
    pServer->startAdvertising();
    Serial.println("Advertising restarted for reconnection");
    advertisingRestarted = true;
  }
};
 
class MyCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic *pCharacteristic) {
    String rxValue = pCharacteristic->getValue();
    if (rxValue.length() > 0) {
      Serial.print("Received via BLE: ");
      Serial.println(rxValue);
    }
  }
};
 
void setup() {
  Serial.begin(115200);
  delay(200);
  Serial.println("Starting ESP32 BLE...");
 
  BLEDevice::init("ESP32_BLE_UART");
 
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
 
  BLEService *pService = pServer->createService(SERVICE_UUID);
 
  pTxCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID_TX,
    BLECharacteristic::PROPERTY_NOTIFY);
  pTxCharacteristic->addDescriptor(new BLE2902());
 
  BLECharacteristic *pRxCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID_RX,
    BLECharacteristic::PROPERTY_WRITE);
  pRxCharacteristic->setCallbacks(new MyCallbacks());
 
  pService->start();
 /*
  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);  // Functions that help with iPhone connections
  pAdvertising->setMinPreferred(0x12);
 */


  pServer->startAdvertising();
  Serial.println("BLE UART service started and advertising...");
 
  //join I2C channel
  Wire.setPins(SDA, SCL);
  Wire.begin();
 
 
 
  //Pollution sensor
  if (!PM.begin_I2C()) {  //connect to sensor over I2C
    Serial.println("Could not find PM25AQI (Pollution)");
    while (1) { delay(100); }
  }
  Serial.println("PM25AQI Found! (Pollution)");
 
  //Pressure sensor
  if (!BAR.begin_I2C()) {  //connect to sensor over I2C
    Serial.println("Could not find LPS22 (Pressure)");
    while (1) { delay(100); }
  }
  BAR.setDataRate(LPS22_RATE_10_HZ);
  Serial.println("LPS22 Found! (Pressure)");
 
  //CO2 Measurement startup
  CARB.begin(Wire, SCD41_I2C_ADDR_62);
 
  if(CARB.wakeUp()){ //returns true if error
    Serial.println("Error SCD41 (CO2) startup");
    while(1){delay(100);}
  }
  delay(100);
 
  while(CARB.startPeriodicMeasurement()){
    CARB.stopPeriodicMeasurement();
    delay(100);
    Serial.println("Error SCD41 (CO2) measurement");
  }
  Serial.println("SDC41 Found! (CO2)");
 
 
  Serial.println("All measurements initialized");
}
 
void loop() {
 
  long timeout = millis() +2000;
 
  // Only send data if connected
  if (deviceConnected) {
 
    //get pollution information
    PM25_AQI_Data pm_data;
 
    while (!PM.read(&pm_data)) {
      Serial.println("Could not read Pollution");
      delay(500);  // try again in a bit!
    }
 
    pm1 = pm_data.pm10_standard;
    pm2_5 = pm_data.pm25_standard;
    pm10 = pm_data.pm100_standard;
   
    Serial.println("Pollution reading success");
 
    //get temp and pressure information
    sensors_event_t temp1;
    sensors_event_t pressure;
    BAR.getEvent(&pressure, &temp1);  // get pressure and temp information
    Serial.println("Pressure reading success");
 
 
    //get carbon information
    while (!co2_ready) {
      delay(100);
      if (CARB.getDataReadyStatus(co2_ready)) {  //true -> error
        Serial.println("Error reading CO2");
        while (1) { delay(100); }
      }
    }
    CARB.readMeasurement(co2, temp2, humid);
   
 
    //pressure, temp
    Serial.printf("Measurement 2:\n");
    Serial.printf("Pressure = %f bar \n", &pressure);
    Serial.printf("Temp = %d°C \n\n", &temp1);
    Serial.print("Temperature: ");
    Serial.print(temp1.temperature);
    Serial.println("°C");
    Serial.print("Pressure: ");
    Serial.print(pressure.pressure);
    Serial.println(" hPa");
 
 
 
    //CO2, humidity, (temp)
    Serial.println("Measurement 3:");
    Serial.printf("CO2 = %d \%", co2);
    Serial.printf("Temp = %f°C \n", temp2);
    Serial.printf("Humidity = %f \%", humid);
 
 
    // Create a string with comma-separated values (Android will parse this as String)
    String dataString =
      String(temp1.temperature, 1)  + "," +
      String(humid, 1)              + "," +
      String(pressure.pressure, 1)  + "," +
      String(co2)                   + "," +
      String(pm1)                   + "," +
      String(pm2_5)                 + "," +
      String(pm10);
 
 
    // Send the string
    pTxCharacteristic->setValue(dataString);
    pTxCharacteristic->notify();

    Serial.println();
    Serial.println("Sent: ");
    Serial.println(dataString);
 
  while(timeout > millis());
 
  }
}