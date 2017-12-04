package main.scala

import scala.collection.mutable.ListBuffer
import akka.actor.{ Actor, ActorRef }
import main.scala.actors.Actuator

class Operation {

  def InitTopics(numtopic: Int): ListBuffer[String] = {
    var topics = new ListBuffer[String]()
    var i = 0
    while (numtopic > i) {

      println("Write Topic" + i.toString())
      topics += readLine()
      i = i + 1
    }
    return topics
  }

  def InitActuators(numact: Int, numtopic: Int): Array[Int] = {
    var topicforact = new Array[Int](numact)
    var i = 0
    var read = 0
    while (numact > i) {
      println("Number topics for actutor" + i.toString())
      read = readInt()
      if (read <= numtopic) {
        topicforact(i) = read
        i = i + 1
      } else
        println("Number of topics for actuator MUST be less than number of topics")
    }
    return topicforact

  }

  def InitSensors(numsens: Int, numtopic: Int): Array[Int] = {
    var topicforsens = new Array[Int](numsens)
    var i = 0
    var read = 0
    while (numsens > i) {
      println("Number topics for sensor" + i.toString())
      read = readInt()
      if (read <= numtopic) {
        topicforsens(i) = read
        i = i + 1
      } else
        println("Number of topics for sensors MUST be less than number of topics")
    }
    return topicforsens

  }

}