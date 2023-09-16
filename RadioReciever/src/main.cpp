#include <Arduino.h>
#include <SPI.h>
#include "printf.h"
#include "RF24.h"
#include <SFE_BMP180.h>
#include <L3G.h>
#include <LSM303.h>
#include <Wire.h>

RF24 radio(49, 53);  // using pin 7 for the CE pin, and pin 8 for the CSN pin
// RF24 radio(7, 8);  // using pin 7 for the CE pin, and pin 8 for the CSN pin
// 7,  8
// 49, 53

#define SIZE 32            
char buffer[SIZE + 1];

SFE_BMP180 bmp180;
L3G l3g;
LSM303 lsm303;

double baseline;
double a;

char report[80];

double getPressure();
double get_averrage_altitude();
void get_compass_data();
void get_gyro_data();
void get_temperature();

void setup() {

  Serial.begin(9600);
  Wire.begin();
  buffer[SIZE] = 0; 
  radio.begin();
  Serial.println(F("RF24/examples/StreamingData"));
  radio.setPALevel(RF24_PA_LOW);
  radio.setPayloadSize(SIZE);
  // radio.openWritingPipe(0xAAAA);
  radio.openReadingPipe(1, 0xFFFF); 
  radio.startListening();
  // For debugging info
  // printf_begin();             // needed only once for printing details
  // radio.printDetails();       // (smaller) function that prints raw register values
  // radio.printPrettyDetails(); // (larger) function that prints human readable data

  bmp180.begin();
  baseline = getPressure();

  l3g.init();
  l3g.enableDefault();

  lsm303.init();
  lsm303.enableDefault();
}
unsigned long t = micros();
unsigned long timer = millis();

void loop() {
  if(radio.available()) {
    radio.read(buffer, SIZE);
    Serial.println(buffer);
    Serial.print("time: ");
    Serial.print(micros() - t);
    Serial.println(" micros");
    t = micros();
  }
  if(timer+2000 < millis()) {
    get_temperature();
    // get_averrage_altitude();
    // get_compass_data();
    // get_gyro_data();
    timer = millis();
  }
  delay(1);
}

void get_gyro_data() {
  l3g.read();

  Serial.print("L3G ");
  Serial.print("X: ");
  Serial.print((int)l3g.g.x);
  Serial.print(" Y: ");
  Serial.print((int)l3g.g.y);
  Serial.print(" Z: ");
  Serial.println((int)l3g.g.z);
}

void get_compass_data() {
  lsm303.read();
  snprintf(report, sizeof(report), "LSM303 A: %6d %6d %6d    M: %6d %6d %6d",
    lsm303.a.x, lsm303.a.y, lsm303.a.z,
    lsm303.m.x, lsm303.m.y, lsm303.m.z);
  Serial.println(report);
}

void get_temperature() {
  char status = bmp180.startTemperature();
  double temp;
  if(status) {
    delay(status);
    bmp180.getTemperature(temp);
    Serial.print("BMP180 C*: ");
    Serial.print(temp);
    Serial.println();
  }
}

double get_averrage_altitude() {
  double a = 0;
  int average = 20;
  for(int i = 0; i < average; i++) {
    a += bmp180.altitude(getPressure(), baseline);
  }
  a /= average;
  Serial.print("BMP180 meters: ");
  Serial.print(a);
  Serial.println();
}

double getPressure() {
  char status;
  double T,P,p0,a;

  status = bmp180.startTemperature();
  if (status != 0) {
    delay(status);
    status = bmp180.getTemperature(T);
    if (status != 0) {
      status = bmp180.startPressure(3);
      if (status != 0) {
        delay(status);
        status = bmp180.getPressure(P,T);
        if (status != 0) {
          return(P);
        } else Serial.println("error retrieving pressure measurement\n");
      } else Serial.println("error starting pressure measurement\n");
    } else Serial.println("error retrieving temperature measurement\n");
  } else Serial.println("error starting temperature measurement\n");
}
