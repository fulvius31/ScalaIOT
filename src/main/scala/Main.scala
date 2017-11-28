
import akka.actor.{ ActorSystem, Props }
import actors.Sensors
import actors.Broker
import actors.Actuator

object Main extends App {
  val system = ActorSystem("ScalaIOT")
  val TopicList  = List("topic1", "")
  val TopicList2 = List("topic1", "topic2","topic1")
  val TopicList3 = List("topic1")

  val actuator = system.actorOf(Actuator.props(0, TopicList), name = "actuator0") :: system.actorOf(
    Actuator.props(1, TopicList2),
    name = "actuator1") :: system.actorOf(Actuator.props(2, TopicList3), name = "actuator2") :: Nil

  val broker = system.actorOf(Broker.props(actuator), name = "broker")

  val sensor0 = system.actorOf(Sensors.props(broker, 0, 10,"topic1" :: "topic2" :: Nil), name = "sensor0")
  val sensor1 = system.actorOf(Sensors.props(broker, 1, 3,"topic3" :: "topic4" :: "topic1" :: Nil), name = "sensor1")

  broker ! "StartMessage"
  Thread.sleep(2000)
  sensor0 ! "StartMessage"
   Thread.sleep(1000)
  sensor1 ! "StartMessage"
 

}
