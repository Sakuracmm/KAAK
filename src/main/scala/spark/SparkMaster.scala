package spark

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

class SparkMaster extends Actor{

  //存储work的信息的
  val id2WorkerInfo = collection.mutable.HashMap[String,WorkerInfo]()


//  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
//    import context.dispatcher
//    context.system.scheduler.schedule(0millis, 6000millis, self,RemoveTimeoutWorker)
//  }

  override def receive: Receive = {
    //会收到worker注册过来的信息
    case RegisterWorkerInfo(wkId,core,ram) => {
      if(!id2WorkerInfo.contains(wkId)) {
        //将worker的信息存储起来，存储到hashMap中
        val workerInfo = new WorkerInfo(wkId, core, ram)
        id2WorkerInfo += ((wkId, workerInfo))

        //master存储完work注册的数据之后，要告诉worker说你已经注册成功
        sender() ! RegisteredWorkerInfo //此时worker会收到注册成功的消息
      }
    }
    case HeartBeat(wkId) =>{
      //master收到worker的心跳消息之后，更新worker的上一次心跳时间
      val workerInfo = id2WorkerInfo(wkId)
      //更改心跳时间
      val currentTime = System.currentTimeMillis()
      workerInfo.lastHeartBeatTime = currentTime
    }

    case CheckTimeoutWorker => {
      import context.dispatcher
      context.system.scheduler.schedule(0 millis, 6000 millis, self,RemoveTimeoutWorker)
    }
    //将HashMap中的所有value都拿出来，查看当前时间和上一次心跳时间的差3000
    case RemoveTimeoutWorker => {
      val workerInfos = id2WorkerInfo.values
      val currentTime = System.currentTimeMillis()

      //过滤超时的worker
      workerInfos
        .filter(wkInfo => currentTime - wkInfo.lastHeartBeatTime > 3000)
        .foreach(wk => id2WorkerInfo.remove(wk.id))
      println(s"--------还剩${id2WorkerInfo.size} 存活的Worker-----------")
    }
  }
}

object SparkMaster{

  def main(args: Array[String]): Unit = {
    //校验参数
    if(args.length != 3){
      println(
        """
          |请输入参数，<host> <port> <masterName>
        """.stripMargin)
      sys.exit()  //退出程序
    }

    var host = args(0)
    var port = args(1)
    var masterName = args(2)

    val config = ConfigFactory.parseString(
      s"""
        |akka.actor.provider="akka.remote.RemoteActorRefProvider"
        |akka.remote.netty.tcp.hostname=$host
        |akka.remote.netty.tcp.port=$port
      """.stripMargin)

    val actorSystem = ActorSystem("sparkMaster",config)
    val masterActorRef = actorSystem.actorOf(Props[SparkMaster],masterName)

    //自己给自己发送消息，启动一个调度器，定期检查hashMap中超时的worker
    masterActorRef ! CheckTimeoutWorker
  }
}


