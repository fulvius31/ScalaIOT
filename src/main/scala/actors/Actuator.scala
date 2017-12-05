package main.scala.actors

import akka.actor.Actor
import java.io.{ File, FileWriter, BufferedWriter, IOException }
import akka.actor.Props
import akka.event.Logging
import main.scala.messages.{ SensorMessage, StartMessage, ConnectA, Ack }
import akka.actor.actorRef2Scala

object Actuator {
  def props(id: Int, topicInterested: List[String]): Props = Props(new Actuator(id, topicInterested))

}
class Actuator(id: Int, topicInterested: List[String]) extends Actor {
  import Actuator._
  
  val log = Logging(context.system, this)
  
  val file: File = new File("Actuator.txt")
  
  val fw: FileWriter = new FileWriter(file, true);
  
  val bw: BufferedWriter = new BufferedWriter(fw);

  def receive = {
    case SensorMessage(topic, value) =>
      println(Console.CYAN + "\tACTUATOR RECEIVED TOPIC: " + topic + "  WITH VALUE: " + value + "\n")
      
      try {
        bw.write(SensorMessage(topic, value).toString() +" for actuator "+ id + "\n")

      } catch {
        case e: IOException => None

      } finally {
        bw.flush()
        Thread.sleep(2000)
        sender ! Ack()
      }

    case StartMessage() =>
      sender() ! ConnectA(id, topicInterested)

  

    case _ => println(Console.CYAN +  "\tACTUATOR RECEIVED UNEXCEPTED MESSAGE \n")
  }

}