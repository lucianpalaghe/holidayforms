#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
#include <MQTT.h>
#include <SPI.h>
#include <MFRC522.h>
#include <FS.h>
#include <time.h>

uint8_t successRead;    // Variable integer to keep if we have Successful Read from Reader
char hexCard[4 * 2 + 1] = {0}; // this will hold a string we build

const char* mqttServer = "soldier.cloudmqtt.com";
const int mqttPort = 14024;
const char* mqttUser = "asukbczm";
const char* mqttPassword = "EAR5krLsBzGz";

const int TIMEZONE = 2;
struct timeval tv;

WiFiClient espClient;
ESP8266WiFiMulti wifiMulti;
MQTTClient mqttClient(2048);

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
  setupNTP();
  Serial.println(F("ESPLogger starting..."));
}

void setupNTP() {
  configTime(TIMEZONE * 3600, 0, "pool.ntp.org", "time.nist.gov");
  Serial.println(F("Setting up NTP"));
  while (!time(nullptr)) {
    Serial.print(".");
    delay(1000);
  }
}

void connect() {
  Serial.print(F("Connecting to WiFi"));
  while (wifiMulti.run() != WL_CONNECTED) {
    delay(1000);
    Serial.print('.');
  }
  Serial.println("\nConnected to " + String(WiFi.SSID()));

  mqttClient.begin(mqttServer, mqttPort, espClient);
  mqttClient.onMessage(messageReceived);

  Serial.println(F("Connecting to MQTT"));
  while (!mqttClient.connect("ESPLogger", mqttUser, mqttPassword, false)) {
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
  configLeds();
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

  if (isCardValid(hexCard)) {
    publishClocking(hexCard);
    successLeds();
  } else {
    Serial.println("Unknown UID: " + String(hexCard));
    failureLeds(3);
  }

  delay(500);
}

void publishClocking(char* hexCard) {
  Serial.println(F("Publishing to MQTT broker..."));
  gettimeofday(&tv, NULL);
  char payload[30] = {0};
  char* separator = ";";
  strcat(payload, hexCard);
  strcat(payload, separator);
  char temp[10];
  ltoa(tv.tv_sec, temp, 10);
  strcat(payload, temp);
  Serial.println(payload);
  boolean succ = mqttClient.publish("clocking", payload, false, 1);
  if (succ) {
    Serial.println(F("Publish successful!"));
  } else {
    failureLeds(5);
    Serial.println(F("Publish failed!"));
  }
}

void successLeds() {
  digitalWrite(LED_BUILTIN, LED_ON);
  delay(800);
  digitalWrite(LED_BUILTIN, LED_OFF);
}

void failureLeds(int flashCount) {
  for (int i = 0; i < flashCount; i++) {
    digitalWrite(LED_BUILTIN, LED_ON);
    delay(150);
    digitalWrite(LED_BUILTIN, LED_OFF);
    delay(150);
  }
}

void configLeds() {
  for (int i = 0; i < 3; i++) {
    digitalWrite(LED_BUILTIN, LED_ON);
    delay(50);
    digitalWrite(LED_BUILTIN, LED_OFF);
    delay(100);
    digitalWrite(LED_BUILTIN, LED_ON);
    delay(50);
    digitalWrite(LED_BUILTIN, LED_OFF);
    delay(50);
    digitalWrite(LED_BUILTIN, LED_ON);
    delay(100);
    digitalWrite(LED_BUILTIN, LED_OFF);
    delay(1000);
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

bool isCardValid(char* uid) {
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
}