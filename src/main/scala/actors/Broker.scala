
package main.scala.actors

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.{ Actor, ActorRef }
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Success }
import akka.actor.Props
import akka.event.Logging
import scala.collection.mutable.ArrayBuffer
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{ Await, Future }
import scala.language.postfixOps
import scala.concurrent.Await
import java.util.concurrent.TimeoutException
import main.scala
import akka.actor.actorRef2Scala
import main.scala.messages._

object Broker {
  def props(actuator: List[ActorRef], numact: Int): Props = Props(new Broker(actuator, numact))
  var iterator_act = 0

}
class Broker(actuator: List[ActorRef], numact: Int) extends Actor {
  import Broker.iterator_act
  
  private val log = Logging(context.system, this)
  //actuator list registered
  private var listA = new ListBuffer[ConnectA]()
  //sensor list registered
  private var listS = new ListBuffer[ConnectS]()
  //received message archive
  private val archive = new ArrayBuffer[String]
  //Timeout
  private val GlobalTimeout = Timeout(2 seconds)
  //sensor and actuator map
  private var attuatori = collection.mutable.Map[Int, List[String]]()
  private val sensori = collection.mutable.Map[String, Int]()

  def receive = {
    case StartMessage() =>
      while(numact > iterator_act){
        actuator(iterator_act) ! StartMessage()
        iterator_act = iterator_act + 1
      }
      
    case ConnectS(id) =>
      println(Console.YELLOW + "\tBROKER RECEIVED CONNECTS FROM SENSOR " + id + "\n")
      listS += ConnectS(id)
      println(Console.YELLOW + "\tSENSOR " + id + " IS REGISTERED \n")
      ReceivedMessageArchive(ConnectS.toString())

    case ConnectA(id, interestedTopic) =>
      println(Console.YELLOW + "\tBROKER RECEIVED CONNECTA FROM ACTUATOR " + id + " WITH INTERESTED TOPIC" + interestedTopic + "\n")
      listA += ConnectA(id, interestedTopic)
      //mapping actuator id -> interestedTopic
      attuatori += (id -> interestedTopic)
      ReceivedMessageArchive(ConnectA.toString())

    case SensorMessage(topic, value) =>
      println(Console.YELLOW + "\tRECEIVED SENSORMESSAGE WITH TOPIC: " + topic + " AND VALUE: " + value + "\n")
      Thread.sleep(1000)

      //Searching topic in all topicLIst, return a list with id of actuators interested
      var option = attuatori.filter(_._2.contains(topic)).map(_._1)

      for (i <- option) {
        println(Console.YELLOW + "\tLIST OF INTERESTED ACTUATOR " + option + "\n")
        println(Console.YELLOW + "\tFORWARDING " + SensorMessage(topic, value).toString + " TO ACTUATOR " + i + "\n")

        var ritrasmissione = RetrasmissionAckTimeoutBased(topic, value, i, 3)

        //log.info("MAPPA: " + attuatori)

        sensori += (topic -> value)
      }

      ReceivedMessageArchive(SensorMessage.toString())

    case _ =>
      println(Console.YELLOW + "\tBROKER RECEIVED UNEXCEPTED MESSAGE  \n")
      ReceivedMessageArchive(Console.YELLOW + "\tRECEIVED UNEXCEPTED MESSAGE \n")
  }
  //Archive all received message
  private def ReceivedMessageArchive(message: String) = {

    archive += message
    println(Console.YELLOW + "\t" + message + " ARCHIVED\n")
  }
  //Retrasmission
  private def RetrasmissionAckTimeoutBased(topic: String, value: Int, idActuator: Int, numRetrasmission: Int): Boolean = {

    try {

      implicit val timeout = GlobalTimeout

      var future: Future[Ack] = ask(actuator(idActuator), SensorMessage(topic, value)).mapTo[Ack]
      val result = Await.result(future, timeout.duration).asInstanceOf[Ack]
      future.onComplete {
        case Success(result) => println(Console.YELLOW + "\tI RECEIVED  : " + result+"\n")
        case Failure(result) => println(Console.YELLOW + "\tFAULT \n")
      }

      true
    } catch {
      case e: TimeoutException =>
        if (numRetrasmission == 0) {
          None
          println(Console.YELLOW + "\tRETRASMISSION ATTEMPTS:" + numRetrasmission + "\n")

          return true
        } else {
          println(Console.YELLOW + "\tBROKER NOT RECEIVED ACK,NON HO RICEVUTO L'ACK, RETRANSMIT  " + numRetrasmission + "\n")
          RetrasmissionAckTimeoutBased(topic, value, idActuator, numRetrasmission - 1)
          return true
        }
    }
  }

}
