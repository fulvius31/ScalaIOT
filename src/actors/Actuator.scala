package actors

import akka.actor.Actor
import java.io._
import akka.actor.Props
import akka.event.Logging
import messages.SensorMessage
import messages.ConnectA
import akka.actor.actorRef2Scala

object Actuator {
  def props(id: Int, topicInterested: List[String]): Props = Props(new Actuator(id, topicInterested))

}
class Actuator(id: Int, topicInterested: List[String]) extends Actor {
  import Actuator._
  val log = Logging(context.system, this)
  val file: File = new File("Actuator.txt")
  val fw: FileWriter = new FileWriter(file, true);
  //BufferedWriter writer give better performance
  val bw: BufferedWriter = new BufferedWriter(fw);

  def receive = {
    case SensorMessage(topic, value) =>
      println("\tACTUATOR RECEIVED TOPIC: " + topic + "  WITH VALUE: " + value + "\n")
      //writing file
      try {
        bw.write(SensorMessage(topic, value).toString() + id + "\n")

      } catch {
        case e: IOException => None

      } finally {
        bw.flush()
        sender ! "ack"
      }

    case "StartMessage" =>
      sender() ! ConnectA(id, topicInterested)

    case "ack" =>
      println("\tACTUATOR  " + id + " IS REGISTERED \n")

    case _ => println("\tRECEIVED UNEXCEPTED MESSAGE \n")
  }

}