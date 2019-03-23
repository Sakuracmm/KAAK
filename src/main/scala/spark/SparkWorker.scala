package spark

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._  //导入时间单位

class SparkWorker(masterUrl: String) extends Actor{

  //master的actorRef对象
  var masterProxy:ActorSelection = _
  var workId = UUID.randomUUID().toString()


  override def preStart(): Unit = {
    masterProxy = context.actorSelection(masterUrl)
  }

  override def receive: Receive = {
    case "started !" => {//自己已就绪
      //worker要向master注册自己的信息，id,core,ram
      println("started !")
      masterProxy ! RegisterWorkerInfo(workId, 4, 1024)   //此时master会收到该条消息
    }
    case RegisteredWorkerInfo =>{ //master发送给自己的注册成功消息
        //worker要启动一个定时器定期地向master发送心跳证明自己活着
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, 1500 millis, self, SendHeartBeat)
    }
    case SendHeartBeat => {
      //开始向master发送心跳了
      println(s"--------------${workId}发送心跳-----------------")
      masterProxy ! HeartBeat(workId) //此时master将会收到心跳消息
    }

  }
}


object SparkWorker{
  def main(args: Array[String]): Unit = {

    //校验参数
    if(args.length != 4){
      println(
        """
          |请输入参数，<host> <port> <workName> <masterURL>
        """.stripMargin)
      sys.exit()  //退出程序
    }

    var host = args(0)
    var port = args(1)
    var workName = args(2)
    var masterURL = args(3)

    val config = ConfigFactory.parseString(
      s"""
        |akka.actor.provider="akka.remote.RemoteActorRefProvider"
        |akka.remote.netty.tcp.hostname=$host
        |akka.remote.netty.tcp.port=$port
      """.stripMargin)
    val actorSystem = ActorSystem("sparkWorker",config)

    //创建自己的actorRef
    val workActorRef = actorSystem.actorOf(Props(new SparkWorker(masterURL)),workName)

    //给自己发送一个已启动的消息，表示自己已经就绪了
    workActorRef ! "started !"
  }
}
