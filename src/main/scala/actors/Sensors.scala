package main.scala.actors

import akka.actor.{ Actor, ActorRef }
import akka.actor.Props
import akka.event.Logging
import java.util.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import main.scala.messages.{ StartMessage, ConnectS, SensorMessage }
import akka.actor.actorRef2Scala

object Sensors {
  def props(broker: ActorRef, id: Int, numTopic: Int, listTopic: List[String], freq: Int): Props = Props(new Sensors(broker, id, numTopic, listTopic, freq))

}
class Sensors(broker: ActorRef, id: Int, numtopic: Int, listTopic: List[String], freq: Int) extends Actor {
  
  val log = Logging(context.system, this)
  val r = scala.util.Random


  override def preStart() {
   val frequency = new FiniteDuration(freq,TimeUnit.SECONDS)
    // scheduling SensorMessage sending schedule
    val MessageScheduling =
      context.system.scheduler.schedule(
        0 milliseconds,
        frequency,
        self,
        "schedule")
  }

  def receive = {

    case StartMessage() =>

      println(Console.GREEN + "\tSENSOR " + id + " SENDING " + numtopic + " TOPIC \n")
      broker ! ConnectS(id)

    case "schedule" =>
      println(Console.GREEN + "\tSCHEDULE SENSOR" + id + "\n")
      //var i = numtopic
      // while (i != 0) {
      //  i = i - 1
      broker ! TopicToSend()
    //}

    case _ => println(Console.GREEN + "\tSENSOR RECEIVED UNEXCEPTED MESSAGE \n" )
  }

  private def TopicToSend(): SensorMessage =
    {
      val rnd = r.nextInt((listTopic.size))

      return SensorMessage(listTopic(rnd), r.nextInt(50) + 50)

    }
}