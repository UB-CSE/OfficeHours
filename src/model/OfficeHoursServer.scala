package model

import com.corundumstudio.socketio
import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}


class OfficeHoursServer() {

  val database: DatabaseAPI = if(model.Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }

  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Int], new ReadyForStudentListener(this))

  server.start()

  def queueJSON(): String = {
    val queue: List[StudentInQueue] = database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

}

object OfficeHoursServer {
  def main(args: Array[String]): Unit = {
    new OfficeHoursServer()
  }
}


class DisconnectionListener(server: OfficeHoursServer) extends DisconnectListener {
  override def onDisconnect(socket: SocketIOClient): Unit = {
    if (server.socketToUsername.contains(socket)) {
      val username = server.socketToUsername(socket)
        server.socketToUsername -= socket
      if (server.usernameToSocket.contains(username)) {
        server.usernameToSocket -= username
      }
    }
  }
}

//Made Changes in EnterQueueListener by introducing preferred TA and if no TA is selected, assign a random TA (Not the professor).

class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    val r = new scala.util.Random
    val TA_list = List("Logan","Megan","John_D","Jon","Joseph","Kevin","Taisia","Snigdha","Shamroy")
    var student_name = username.split(",").head
    var ta_name = username.split(",")(1)
    if(ta_name == "any"){
      ta_name = (TA_list)(r.nextInt(TA_list.length))
    }
    val structured = student_name + "," + ta_name


    server.database.addStudentToQueue(StudentInQueue(structured, System.nanoTime()))
    server.socketToUsername += (socket -> structured)
    server.usernameToSocket += (structured -> socket)
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}

// Made changes in ReadyForStudentListener. It now takes an Int of the RowIndex to remove (Student who have completed his OH).
class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Int] {
  override def onData(socket: SocketIOClient, dirtyMessage: Int, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      println(dirtyMessage)
      val studentToHelp = queue(dirtyMessage)
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}


