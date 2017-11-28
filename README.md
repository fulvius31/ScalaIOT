# ScalaIOT
Project for simple implentation in Scala of MQTT protocol, using AKKA for University of Catania Homework (Advanced Computer Language).
Implemented three actors:
  - Sensor
  - Actuator
  - Broker
  
Sensor sends Messages to Broker with various topics. Every Actuator is subscribed to some topics and then Broker will periodically sends Messages with Topic to subscribed Actuator.
Finally Actuators print received messages and save them into a file (Actuator.txt).
