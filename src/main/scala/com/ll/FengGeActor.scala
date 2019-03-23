package com.ll

import akka.actor.Actor

/**
  * 峰哥
  */
class FengGeActor extends Actor{
  override def receive: Receive = {
    case "start" => println("峰峰说：I'm Ok")
    case "啪"  => {
      println("峰峰：看好,接球！")
      Thread.sleep(1000)
      sender() ! "崩崩"
    }
  }
}
