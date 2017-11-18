import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging

class Sensors(broker: ActorRef) extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case "inizio" => 
      broker ! SensorMessage("asd",1)
      
    case _ => log.info("received unexcepted message")
   }
}