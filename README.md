# ScalaIOT
Project for simple implentation in Scala of MQTT protocol, using AKKA for University of Catania Homework (Advanced Computer Language).
Implemented three actors:
  - Sensor
  - Actuator
  - Broker
  
Sensor sends Messages to Broker with various topics. Every Actuator is subscribed to some topics and then Broker will periodically sends Messages with Topic to subscribed Actuator.
Finally Actuators print received messages and save them into a file (Actuator.txt).

# Dependencies (Debian)

sbt is necessary for starting project.
For Debian OS based:

    echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
    sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
    sudo apt-get update
    sudo apt-get install sbt
    
 # How to start
 
 In the path Project:
    $sbt compile run
