#include <Arduino.h>
#include <SPI.h>
// #include <digitalWriteFast.h>
#include "IBoardRF24.h"

// instantiate an object for the nRF24L01 transceiver
iBoardRF24 radio(3, 8, 5, 6, 7, 2);
// RF24 radio(49, 53);  // using pin 7 for the CE pin, and pin 8 for the CSN pin
// RF24 radio(7, 8);  // using pin 7 for the CE pin, and pin 8 for the CSN pin
// 7,  8
// 49, 53


// Let these addresses be used for the pair
#define SIZE 32 
char buffer[SIZE + 1] = "Kek";
void makePayload(uint8_t);
void sendData();

void setup() {
  // buffer[SIZE] = 0;
  
  Serial.begin(9600);
  radio.begin();
  Serial.println(F("RF24/examples/StreamingData"));
  radio.setPALevel(RF24_PA_LOW); 
  radio.setPayloadSize(SIZE);
  radio.openWritingPipe(0xFFFF);
  radio.openReadingPipe(1, 0xAAAA);
  radio.startListening();

  // For debugging info
  // printf_begin();             // needed only once for printing details
  // radio.printDetails();       // (smaller) function that prints raw register values
  // radio.printPrettyDetails(); // (larger) function that prints human readable data

}


void loop() {
  sendData();
  // if (radio.available()) {
    // radio.read(&buffer, SIZE);
    // Serial.print(F("Received: "));
    // Serial.println(buffer);
  // }
}

unsigned long t = 0;

void sendData() {
  if (t+50 < millis()) {
    radio.stopListening();
    radio.write(buffer, 3);
    radio.startListening();
    t = millis();
  }
}