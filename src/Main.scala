
import akka.actor.{ActorSystem, Props}

object Main extends App {
	val system = ActorSystem("HelloSystem")

	val actuator = system.actorOf(Props[Actuator], name="Actuator")
	val broker = system.actorOf(Props(new Broker(actuator)), name="broker")
	val sensor = system.actorOf(Props(new Sensors(broker)), name="sensor")

	sensor ! "inizio"
}
