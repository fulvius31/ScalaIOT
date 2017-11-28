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
  //  val out = new PrintWriter(new File("Actuator.txt"))
  val file: File = new File("Actuator.txt")
  val fw: FileWriter = new FileWriter(file, true);
  //BufferedWriter writer give better performance
  val bw: BufferedWriter = new BufferedWriter(fw);
  //Closing BufferedWriter Stream

  def receive = {
    case SensorMessage(topic, value) =>
      log.info("HO RICEVUTO IL SEGUENTE TOPIC: " + topic + "  CON IL VALORE: " + value)
      try {
        bw.write(SensorMessage(topic, value).toString() + id + "\n")

      } catch {
        case e: IOException => None

      } finally {
        bw.flush()
        //bw.close()
        sender ! "ack"
      }

      // out.write(SensorMessage(topic, value).toString() + "\n")
      // out.close()
      // Thread.sleep(3000)

    case "inizio" =>
      sender() ! ConnectA(id, topicInterested)

    case "ack" =>
      log.info("actuator" + id + " registered")

    case _ => log.info("received unexcepted message")
  }

}