package com.ll

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

class HelloActor extends Actor{

  //用来接收消息的
  override def receive: Receive = {
    //接收消息并处理
    case "你好帅"  => println("净说实话，我喜欢你这种人！")
    case "丑"  => println("滚犊子")

    case "stop" => {
      context.stop(self)  //停止自己的actorRef
      context.system.terminate()  //关闭ActorSystem
    }
  }
}

object HelloActor {

  private val nBFactory = ActorSystem("NBFactory")//工厂
  private val helloActorRef: ActorRef = nBFactory .actorOf(Props[HelloActor],"helloActor")

  def main(args: Array[String]): Unit = {
    //给自己发送消息
    helloActorRef ! "你好帅"
    helloActorRef ! "丑"

    helloActorRef ! "stop"
  }

}
