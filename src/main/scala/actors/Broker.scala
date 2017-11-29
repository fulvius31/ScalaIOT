
package main.scala.actors

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.Actor
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }
import akka.actor.ActorRef
import akka.actor.Props
import akka.event.Logging
import scala.collection.mutable.ArrayBuffer
import akka.pattern.ask
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await
import java.util.concurrent.TimeoutException
import main.scala
import akka.actor.actorRef2Scala
import main.scala.messages._
object Broker {
  def props(actuator: List[ActorRef]): Props = Props(new Broker(actuator))

}
class Broker(actuator: List[ActorRef]) extends Actor {
  val log = Logging(context.system, this)
  //actuator list registered
  var listA = new ListBuffer[ConnectA]()
  //sensor list registered
  var listS = new ListBuffer[ConnectS]()
  //received message archive
  val archive = new ArrayBuffer[String]
  //Timeout
  val GlobalTimeout = Timeout(2 seconds)
  //sensor and actuator map
  var attuatori = collection.mutable.Map[Int, List[String]]()
  val sensori = collection.mutable.Map[String, Int]()

  def receive = {
    case StartMessage() =>
      actuator(0) ! StartMessage()
      actuator(1) ! StartMessage()
      actuator(2) ! StartMessage()

    case ConnectS(id) =>
      println("\tBROKER RECEIVED CONNECTS FROM SENSOR " + id + "\n")
      listS += ConnectS(id)
      println("\tSENSOR " + id + " IS REGISTERED \n")
      ReceivedMessageArchive(ConnectS.toString())

    case ConnectA(id, interestedTopic) =>
      println("\tBROKER RECEIVED CONNECTA FROM ACTUATOR " + id + " WITH INTERESTED TOPIC" + interestedTopic + "\n")
      listA += ConnectA(id, interestedTopic)
      //mapping actuator id -> interestedTopic
      attuatori += (id -> interestedTopic)
      ReceivedMessageArchive(ConnectA.toString())
      sender() ! "ack"

    case SensorMessage(topic, value) =>
      println("\tRECEIVED SENSORMESSAGE WITH TOPIC: " + topic + " AND VALUE: " + value + "\n")
      Thread.sleep(1000)

      //Searching topic in all topicLIst, return a list with id of actuators interested
      var option = attuatori.filter(_._2.contains(topic)).map(_._1)

      for (i <- option) {
        println("\tLIST OF INTERESTED ACTUATOR " + option + "\n")
        println("\tFORWARDING " + SensorMessage(topic, value).toString + " TO ACTUATOR " + i + "\n")

        var ritrasmissione = RetrasmissionAckTimeoutBased(topic, value, i, 3)

        //log.info("MAPPA: " + attuatori)

        sensori += (topic -> value)
      }

      ReceivedMessageArchive(SensorMessage.toString())

    case _ =>
      println("\tBROKER RECEIVED UNEXCEPTED MESSAGE  \n")
      ReceivedMessageArchive("\tRECEIVED UNEXCEPTED MESSAGE \n")
  }
  //Archive all received message
  def ReceivedMessageArchive(message: String) = {

    archive += message
    println("\t" + message + " ARCHIVED\n")
  }
  //Retrasmission
  private def RetrasmissionAckTimeoutBased(topic: String, value: Int, idActuator: Int, numRetrasmission: Int): Boolean = {

    try {

      implicit val timeout = GlobalTimeout

      var future: Future[String] = ask(actuator(idActuator), SensorMessage(topic, value)).mapTo[String]
      val result = Await.result(future, timeout.duration).asInstanceOf[String]
      future.onComplete {
        case Success(result) => println("\tHO RICEVUTO IL MESSAGGIO : " + result+"\n")
        case Failure(result) => println("\tFAULT \n")
      }

      true
    } catch {
      case e: TimeoutException =>
        if (numRetrasmission == 0) {
          None
          println("\tRETRASMISSION ATTEMPTS:" + numRetrasmission + "\n")

          return true
        } else {
          println("\tBROKER NOT RECEIVED ACK,NON HO RICEVUTO L'ACK, RETRANSMIT  " + numRetrasmission + "\n")
          RetrasmissionAckTimeoutBased(topic, value, idActuator, numRetrasmission - 1)
          return true
        }
    }
  }

}