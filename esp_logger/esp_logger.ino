#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <MQTT.h>
#include <SPI.h>
#include <MFRC522.h>
#include <FS.h>
#include <time.h>

#define LED_ON LOW
#define LED_OFF HIGH
#define redLed D0
#define greenLed D1
#define blueLed D2

uint8_t successRead;    // Variable integer to keep if we have Successful Read from Reader
char hexCard[4 * 2 + 1] = {0}; // this will hold a string we build

const char* mqttServer = "soldier.cloudmqtt.com";
const int mqttPort = 14024;
const char* mqttUser = "asukbczm";
const char* mqttPassword = "EAR5krLsBzGz";

WiFiClient espClient;
ESP8266WiFiMulti wifiMulti;
MQTTClient mqttClient(1024);

#define RST_PIN D2
#define SS_PIN D8
MFRC522 mfrc522(SS_PIN, RST_PIN);

void setup() {
  Serial.begin(115200);
  setupLeds();
  SPI.begin();
  mfrc522.PCD_Init();

  wifiMulti.addAP("PSS_Brasov", "Ciccio123!");   // add Wi-Fi networks you want to connect to
  wifiMulti.addAP("AndroidAP44EF", "qjcm6009");
  wifiMulti.addAP("Etaj12", "jungle9998");
  Serial.println();
  boolean result = SPIFFS.begin();
  if (result) {
    Serial.println(F("File system mounted with success"));
  } else {
    Serial.println(F("Error mounting the file system"));
  }

  Serial.println(F("Logger starting..."));
}

void connectTime() {
  configTime(3 * 3600, 0, "pool.ntp.org", "time.nist.gov");
  Serial.println("\nWaiting for time");
  while (!time(nullptr)) {
    Serial.print(".");
    delay(1000);
  }
}

void connect() {
  Serial.print(F("Connecting to WiFi..."));
  while (wifiMulti.run() != WL_CONNECTED) {
    delay(1000);
    Serial.print('.');
  }
  Serial.println("\nConnected to " + String(WiFi.SSID()));

  mqttClient.begin(mqttServer, mqttPort, espClient);
  mqttClient.onMessage(messageReceived);

  Serial.println(F("Connecting to MQTT..."));
  while (!mqttClient.connect("ESP8266Thing", mqttUser, mqttPassword, false)) {
    Serial.print(".");
    delay(1000);
  }
  Serial.println("Connected to " + String(mqttServer) + ":" + String(mqttPort));

  mqttClient.subscribe("config");
}

void messageReceived(String &topic, String &payload) {
  Serial.println("Incoming config data: " + payload);

  File uids = SPIFFS.open("/uids.conf", "w+");

  if (!uids) {
    Serial.println(F("Failed to open uids.conf"));
  } else {
    Serial.println(F("Writing uids..."));
    char temp[payload.length() + 1];
    payload.toCharArray(temp, payload.length() + 1);
    char* ptr = strtok(temp, ",");
    while (ptr != NULL) {
      Serial.println("Writing UID: " + String(ptr));
      uids.println(ptr);
      ptr = strtok(NULL, ",");
    }
    uids.close();
  }
}

void loop () {
  mqttClient.loop();
  delay(10);
  if (!mqttClient.connected()) {
    connect();
  }

  successRead = readCard();
  delay(10);

  if (!successRead) {
    return;
  }

  if (validCard(hexCard)) {
    publishClocking(hexCard);
    successLeds();
  } else {
    Serial.println("Unknown UID: " + String(hexCard));
    time_t now = time(nullptr);
    Serial.println(ctime(&now));
    struct tm * timeinfo;

  time (&rawtime);
  timeinfo = localtime (&rawtime);
    failureLeds(3);
  }

  delay(500);
}

void publishClocking(char* hexCard) {
  Serial.println(F("Publishing to MQTT broker..."));
  boolean succ = mqttClient.publish("clocking", hexCard, false, 1);
  if (succ) {
    Serial.println(F("Publish successful!"));
  } else {
    failureLeds(5);
    Serial.println(F("Publish failed!"));
  }

}

void successLeds() {
  //  digitalWrite(blueLed, LED_OFF);
  //  digitalWrite(redLed, LED_OFF);
  //  digitalWrite(greenLed, LED_ON);
  //  delay(1500);
  //  digitalWrite(blueLed, LED_ON);
  //  digitalWrite(redLed, LED_OFF);
  //  digitalWrite(greenLed, LED_OFF);
  //  Serial.println(F("Flashing builtin led"));
  digitalWrite(LED_BUILTIN, LED_ON);
  delay(800);
  digitalWrite(LED_BUILTIN, LED_OFF);
}

void failureLeds(int flashCount) {
  //  digitalWrite(greenLed, LED_OFF);
  //  digitalWrite(blueLed, LED_OFF);
  //  digitalWrite(redLed, LED_ON);
  //  delay(1500);
  //  digitalWrite(blueLed, LED_ON);
  //  digitalWrite(redLed, LED_OFF);
  //  digitalWrite(greenLed, LED_OFF);
  for (int i = 0; i < flashCount; i++) {
    digitalWrite(LED_BUILTIN, LED_ON);
    delay(150);
    digitalWrite(LED_BUILTIN, LED_OFF);
    delay(150);
  }
}


uint8_t readCard() {
  if (!mfrc522.PICC_IsNewCardPresent()) { //If a new PICC placed to RFID reader continue
    return 0;
  }
  if (!mfrc522.PICC_ReadCardSerial()) {   //Since a PICC placed get Serial and continue
    return 0;
  }

  char buildBuffer[4] = {0};
  memset(hexCard, 0, sizeof(hexCard)); // reset the char array

  for (uint8_t i = 0; i < 4; i++) {
    sprintf(buildBuffer, "%02X", (uint8_t)mfrc522.uid.uidByte[i]);
    strcat(hexCard, buildBuffer);
  }
  Serial.print(F("Scanned PICC's UID: "));
  Serial.println(hexCard);
  mfrc522.PICC_HaltA();
  return 1;
}

void cycleLeds() {
  digitalWrite(redLed, LED_OFF);  // Make sure red LED is off
  digitalWrite(greenLed, LED_ON);   // Make sure green LED is on
  digitalWrite(blueLed, LED_OFF);   // Make sure blue LED is off
  delay(200);
  digitalWrite(redLed, LED_OFF);  // Make sure red LED is off
  digitalWrite(greenLed, LED_OFF);  // Make sure green LED is off
  digitalWrite(blueLed, LED_ON);  // Make sure blue LED is on
  delay(200);
  digitalWrite(redLed, LED_ON);   // Make sure red LED is on
  digitalWrite(greenLed, LED_OFF);  // Make sure green LED is off
  digitalWrite(blueLed, LED_OFF);   // Make sure blue LED is off
  delay(200);
}

bool validCard(char* uid) {
  File uidsFile = SPIFFS.open("/uids.conf", "r");
  String line;
  while (uidsFile.available()) {
    line = uidsFile.readStringUntil('\n');
    char temp[line.length()];
    line.toCharArray(temp, line.length());
    if (strcmp(temp, hexCard) == 0) {
      Serial.println(F("UID found..."));
      return true;
    }
  }
  return false;
}

void setupLeds() {
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, HIGH);
  pinMode(redLed, OUTPUT);
  pinMode(greenLed, OUTPUT);
  pinMode(blueLed, OUTPUT);

  digitalWrite(redLed, LED_OFF);
  digitalWrite(greenLed, LED_OFF);
  digitalWrite(blueLed, LED_OFF);
}

void showSpectrum() {
  for (int x = 0; x < 768; x++) {
    showRGB(x);
    delay(10);
  }
}

void showRGB(int color) {
  int redIntensity;
  int greenIntensity;
  int blueIntensity;

  // Here we'll use an "if / else" statement to determine which
  // of the three (R,G,B) zones x falls into. Each of these zones
  // spans 255 because analogWrite() wants a number from 0 to 255.

  // In each of these zones, we'll calculate the brightness
  // for each of the red, green, and blue LEDs within the RGB LED.

  if (color <= 255)          // zone 1
  {
    redIntensity = 255 - color;    // red goes from on to off
    greenIntensity = color;        // green goes from off to on
    blueIntensity = 0;             // blue is always off
  }
  else if (color <= 511)     // zone 2
  {
    redIntensity = 0;                     // red is always off
    greenIntensity = 255 - (color - 256); // green on to off
    blueIntensity = (color - 256);        // blue off to on
  }
  else // color >= 512       // zone 3
  {
    redIntensity = (color - 512);         // red off to on
    greenIntensity = 0;                   // green is always off
    blueIntensity = 255 - (color - 512);  // blue on to off
  }

  // Now that the brightness values have been set, command the LED
  // to those values

  analogWrite(redLed, redIntensity);
  analogWrite(blueLed, blueIntensity);
  analogWrite(greenLed, greenIntensity);
}
