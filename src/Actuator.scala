import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging

class Actuator extends Actor {
 
  val log = Logging(context.system, this)
  def receive = {
    case SensorMessage(topic, value) => println("i received "+topic)
    case _ => log.info("received unexcepted message")
   }
}