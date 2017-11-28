
package actors

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
import messages.ConnectS
import messages.SensorMessage
import messages.ConnectA
import akka.actor.actorRef2Scala

object Broker {
  def props(actuator: List[ActorRef]): Props = Props(new Broker(actuator))

}
class Broker(actuator: List[ActorRef]) extends Actor {
  val log = Logging(context.system, this)
  //lista attuatori registrati
  var listA = new ListBuffer[ConnectA]()
  //lista sensori registrati
  var listS = new ListBuffer[ConnectS]()
  //Archivio messaggi ricevuti
  val archive = new ArrayBuffer[String]
  //Timeout
  val GlobalTimeout = Timeout(2 seconds)
  //map attori e sensori
  var attuatori = scala.collection.mutable.Map[Int, List[String]]()
  val sensori = scala.collection.mutable.Map[String, Int]()

  def receive = {
    case "inizio" =>
      actuator(0) ! "StartMessage"
      actuator(1) ! "StartMessage"
      actuator(2) ! "StartMessage"

    case ConnectS(id) =>
      println("\tBROKER RECEIVED CONNECTS FROM SENSOR " + id + "\n")
      listS += ConnectS(id)
      println("\tSENSOR " + id + " IS REGISTERED \n")
      sender() ! "ack"
      ReceivedMessageArchive(ConnectS.toString())

    case ConnectA(id, interestedTopic) =>
      println("\tBROKER RECEIVED CONNECTA FROM ACTUATOR " + id + " WITH INTERESTED TOPIC" + interestedTopic + "\n")
      listA += ConnectA(id, interestedTopic)
      //mappa gli attuatori come id interestedTopic
      attuatori += (id -> interestedTopic)
      ReceivedMessageArchive(ConnectA.toString())
      sender() ! "ack"

    case SensorMessage(topic, value) =>
      println("\tRECEIVED SENSORMESSAGE WITH TOPIC: " + topic + " AND VALUE: " + value + "\n")
      Thread.sleep(1000)

      //Ricerca degli attuatori interessati al topic
      var option = attuatori.filter(_._2.contains(topic)).map(_._1)

      for (i <- option) {
        println("\tLIST OF INTERESTED ACTUATOR " + option + "\n")
        println("\tFORWARDING " + SensorMessage(topic, value).toString + " TO ACTUATOR " + i + "\n")

        //topic del messaggio, valore, indice dell'attuatore interessato, numero tentativi.
        var ritrasmissione = RetrasmissionAckTimeoutBased(topic, value, i, 3)

        //log.info("MAPPA: " + attuatori)

        sensori += (topic -> value)
      }

    //   ReceivedMessageArchive(SensorMessage.toString())

    case _ =>
      println("\tRECEIVED UNEXCEPTED MESSAGE \n")
      ReceivedMessageArchive("\tRECEIVED UNEXCEPTED MESSAGE \n")
  }

  def ReceivedMessageArchive(message: String) = {

    archive += message
    println("\t"+message + "  ARCHIVED\n")
  }

  private def RetrasmissionAckTimeoutBased(topic: String, value: Int, i: Int, num: Int): Boolean = {

    try {

      implicit val timeout = GlobalTimeout

      var future: Future[String] = ask(actuator(i), SensorMessage(topic, value)).mapTo[String]
      val result = Await.result(future, timeout.duration).asInstanceOf[String]
      future.onComplete {
        case Success(result) => println("HO RICEVUTO IL MESSAGGIO : " + result)
        case Failure(result) => println("FAULT ")
      }

      true
    } catch {
      case e: TimeoutException =>
        if (num == 0) {
          None
          println("\tRETRASMISSION ATTEMPTS:"+num+"\n")

          return true
        } else {
          println("\tBROKER NOT RECEIVED ACK,NON HO RICEVUTO L'ACK, RETRANSMIT  " + num+"\n")
          RetrasmissionAckTimeoutBased(topic, value, i, num - 1)
          return true
        }
    }
  }

}