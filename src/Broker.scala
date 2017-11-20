import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging

object Broker{
  def props(actuator: List[ActorRef]): Props = Props(new Broker(actuator))
  
}
class Broker(actuator: List[ActorRef]) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case "inizio" =>
      actuator(0) ! "inizio"
      actuator(1) ! "inizio"
      actuator(2) ! "inizio"
    case SensorMessage(topic,id) =>
      log.info("broker received topic "+topic+" from sensor "+id)
      val sensori = Map (topic -> id)
      sender() ! "ack"
    case ActuatorMessage(interestedTopic,id) =>
      log.info("broker received topic "+interestedTopic+" from actuator "+id)
      val attuatori = Map (interestedTopic -> id)
      sender() ! "ack"
    case _ => log.info("received unexcepted message")
   }
}