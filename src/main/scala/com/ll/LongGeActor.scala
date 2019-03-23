package com.ll

import akka.actor.{Actor, ActorRef}

class LongGeActor(val fg: ActorRef) extends Actor{
  //接收消息
  override def receive: Receive = {
    case "start" => {
      println("龙龙：I'm Ok")
      fg ! "啪"
    }
    case "崩崩" =>{
      println("龙龙：你真猛！")
      Thread.sleep(1000)
      fg ! "啪"
    }
  }
}
