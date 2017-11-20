import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging

object Sensors{
  def props(broker: ActorRef, id: String, numTopic: String): Props = Props(new Sensors(broker, id ,numTopic))
  
}
class Sensors(broker: ActorRef,id: String, numtopic: String) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case "inizio" => 
      broker ! SensorMessage("temperatura", id)
    case "ack" =>
      log.info("sensor" +id+" registered")
    case _ => log.info("received unexcepted message")
   }
}