
import akka.actor.{ActorSystem, Props}

object Main extends App {
	val system = ActorSystem("ScalaIOT")
	
	val proprieties = system.actorOf(Actuator.props("0", "temperatura"), name="actuator0") :: system.actorOf(Actuator.props("1", "temperatura"), name="actuator1") :: system.actorOf(Actuator.props("2", "temperatura"), name="actuator2") :: Nil
	
	val actuator = proprieties

	val broker =   system.actorOf(Broker.props(actuator), name="broker")

	val sensor0 =  system.actorOf(Sensors.props(broker, "0", "1"), name="sensor0")
	val sensor1 =  system.actorOf(Sensors.props(broker, "1", "2"), name="sensor1")

		

	sensor0  ! "inizio"
	sensor1  ! "inizio"
	broker   ! "inizio"

}
