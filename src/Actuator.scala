import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging

object Actuator{
  def props(id: String, topicInterested: String): Props = Props(new Actuator(id ,topicInterested))
  
}
class Actuator(id: String, topicInterested: String) extends Actor {

  val log = Logging(context.system, this)
  def receive = {
    case SensorMessage(topic, value) => println("i received "+topic)
    case "inizio" => 
      sender() ! ActuatorMessage(id, topicInterested)
    case "ack" =>
      log.info("actuator" +id+" registered")
    case _ => log.info("received unexcepted message")
   }
}