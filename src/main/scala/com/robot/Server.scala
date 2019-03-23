package com.robot

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

class Server extends Actor{

  //用来接收客户端发送过来的问题
  override def receive: Receive = {
    case "start"  => println("老年已启动！")
    case ClientMessage(msg) => {
      println(s"收到客户端消息：$msg")
      msg match {
        case "你叫啥" => sender() ! ServerMessage("铁扇公主")
        case "你是男是女" => sender() ! ServerMessage("老娘是男的")
        case "你男朋友吗" => sender() ! ServerMessage("没有")
        case _ => sender() ! ServerMessage("What you said ?")   //sender()发送的代理对象，发送到客户端的mailbox -> 调用客户端的receive方法
      }
    }
  }

}

object Server extends App{

  val host:String = "127.0.0.1"
  val port:Int = 8878

  private val config: Config = ConfigFactory.parseString(
    s"""
akka.actor.provider = "akka.remote.RemoteActorRefProvider"
akka.remote.netty.tcp.hostname = $host
akka.remote.netty.tcp.port = $port
    """.stripMargin)

  //指定IP和端口号
  private val actorSystem = ActorSystem("Server",config)
  private val serverActorRef: ActorRef = actorSystem.actorOf(Props[Server],"shanghai")
  serverActorRef ! "start"
}
