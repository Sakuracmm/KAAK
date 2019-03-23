package com.robot

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

import scala.io.StdIn

class Client2 extends Actor{


  var serverActorRef: ActorSelection = _ //服务端的代理对象

  override def preStart(): Unit = {
    serverActorRef = context.actorSelection("akka.tcp://Server@127.0.0.1:8878/user/shanghai")
  }

  override def receive: Receive = {
    case "start" => println("客户端启动程序......")
    case msg:String => {
      serverActorRef ! ClientMessage(msg)    //把客户端输入的内容发送给服务端的mailbox，发送的内容为一个样例类，实现了serializable（序列化接口），和Product
    }
    case ServerMessage(msg) => println(s"接收到服务端消息： $msg")
  }
}
object Client2 extends App{

  val host:String = "127.0.0.1"
  val port:Int = 8880

  private val config: Config = ConfigFactory.parseString(
    s"""
       akka.actor.provider = "akka.remote.RemoteActorRefProvider"
       akka.remote.netty.tcp.hostname = $host
       akka.remote.netty.tcp.port = $port
    """.stripMargin)

  private val clientSystem = ActorSystem("client",config)

  private val actorRef: ActorRef = clientSystem.actorOf(Props[Client2],"client1")

  actorRef ! "start"  //发送到自己的mailBox，调用自己的receive方法

  while(true){
    val question = StdIn.readLine() //同步阻塞
    actorRef ! question //发送到自己的mailBox -> 调用自己的receive方法
  }

}



