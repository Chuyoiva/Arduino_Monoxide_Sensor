/* 
 MQ9 
 modified on 19 Feb 2019 
 by Saeed Hosseini 
 https://electropeak.com/learn/ 
*/ 
#include<math.h>
#include "DHT.h"

#define DHTPIN 7
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

void setup() { 
 Serial.begin(9600); 
 dht.begin();
} 
void loop() { 
 
 float sensor_volt = 0.0; 
 float RS_gas = 0.0; 
 float ratio = 0.0;
 double ppm = 0.0;
 //-Replace the name "R0" with the value of R0 in the demo of First Test -/ 
 float R0 = 2.85; 
 float x = 0.0;
 int sensorValue = analogRead(A0); 
 sensor_volt = ((float)sensorValue / 1024.0) * 5; 
  RS_gas = (5.0 - sensor_volt) / sensor_volt; // Depend on RL on yor module 
 ratio = RS_gas / R0; // ratio = RS/R0 
 //574.15x-2.254
 x = pow(ratio, - 2.254);//2.095);//-1.996
 ppm = 574.15*x;//608.36 * x; //608.05
//------------------------------------------------------------/ 
  float h = dht.readHumidity();
  float t = dht.readTemperature();
  float f = dht.readTemperature(true);

  if (isnan(h) || isnan(t) || isnan(f)) {
    Serial.println(F("Failed to read from DHT sensor!"));
    return;
  }
  float hif = dht.computeHeatIndex(f, h);  
  float hic = dht.computeHeatIndex(t, h, false);
//------------------------------------------------------------/ 
 //Serial.print("sensor_volt = "); 
 //Serial.println(sensor_volt); 
 //Serial.print("RS_ratio = "); 
 //Serial.println(RS_gas);   
 //Serial.print("Rs/R0 = "); 
 //Serial.println(ratio); 
 //Serial.print("CO = "); 
 Serial.print(ppm);
 Serial.print(",");
 Serial.print(t);
 Serial.print(",");
 Serial.print(h);
 Serial.print("#");
 Serial.println();
 //Serial.println("ppm");  
 //Serial.print("\n\n"); 
 
 delay(1000); 
}
