package spark

/**
  * worker -> master
  * @param id
  * @param core
  * @param ram
  */
//work向master注册自己的信息
case class RegisterWorkerInfo(id:String,core:Int,ram: Int)

//worker给mater发送心跳信息
case class HeartBeat(id:String)


/*
 * master -> worker
 */
//master向woker发送注册成功消息
case object RegisteredWorkerInfo

//worker发送给自己的消息告诉自己说要开始周期性地向master发送心跳消息
case object SendHeartBeat

//master自己给自己发送一个检查超时worker的信息,并启动一个调度器，周期性检测删除超时worker
case object CheckTimeoutWorker
//master发送给自己的消息，删除超时的worker
case object RemoveTimeoutWorker

//存储worker信息的类
class WorkerInfo(val id: String, core:Int, ram: Int){
  var lastHeartBeatTime: Long = _
}