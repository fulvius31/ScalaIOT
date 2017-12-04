import akka.actor.{ ActorSystem, Props }
import main.scala.actors.{ Actuator, Broker, Sensors }
import main.scala.messages.StartMessage
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import akka.actor.ActorRef
import main.scala.Operation
import main.scala.Operation

object Main extends App {

  val operation = new Operation
  println("Number of Topics :")
  val numtopic = readInt()
  val topics = operation.InitTopics(numtopic)

  println("Number of actuators :")
  val numact = readInt()
  val topicforact = operation.InitActuators(numact,numtopic)

  val TopicList = topics.toList

  val system = ActorSystem("ScalaIOT")

  var actuators = new ListBuffer[ActorRef]

  var i = 0
  while (numact > i) {
    actuators += system.actorOf(Actuator.props(i, TopicList.take(topicforact(i))), name = "actuator" + (i).toString())
    i = i + 1
  }

  i = 0
  println("Number of sensors :")
  val numsens = readInt()
  val topicforsens = operation.InitSensors(numsens,numtopic)
  val freq = operation.InitFreq(numsens)
  var sensors = new ListBuffer[ActorRef]

  val actuator = actuators.toList
  val broker = system.actorOf(Broker.props(actuator, numact), name = "broker")

  while (numsens > i) {

    sensors += system.actorOf(Sensors.props(broker, i, topicforsens(i), TopicList.take(topicforsens(i)), freq(i)), name = "sensor" + i.toString())
    i = i+1
  }

  val sensor = sensors.toList
 
  broker ! StartMessage()

  i = 0
  
  while(numsens > i){
    sensor(i) ! StartMessage()
    i = i+1
  }
}