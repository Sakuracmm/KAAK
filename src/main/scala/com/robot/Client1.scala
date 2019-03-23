package com.robot

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scala.io.StdIn

class Client1 extends Actor{


  var serverActor: ActorSelection = _

  override def preStart(): Unit = {
    serverActor = context.actorSelection("akka.tcp://Server@127.0.0.1:8878/user/shanghai")
  }

  override def receive: Receive = {
    case "start" => println("客户端启动程序......")
    case msg:String => {
      serverActor ! ClientMessage(msg)
    }
    case ServerMessage(msg) => println(s"接收到服务端消息： $msg")
  }
}

object Client1 extends App{

  val host:String = "127.0.0.1"
  val port:Int = 8879

  private val config: Config = ConfigFactory.parseString(
    s"""
       akka.actor.provider = "akka.remote.RemoteActorRefProvider"
       akka.remote.netty.tcp.hostname = $host
       akka.remote.netty.tcp.port = $port
    """.stripMargin)

  private val clientSystem = ActorSystem("client",config)

  private val actorRef: ActorRef = clientSystem.actorOf(Props[Client1],"client1")

  actorRef ! "start"

  while(true){
    val question = StdIn.readLine()
    actorRef ! question
  }

}
