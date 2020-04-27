package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}


class OfficeHoursServer() {

  val database: DatabaseAPI = if(Configuration.DEV_MODE){
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
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("leave_queue", classOf[Nothing], new LeaveQueueListener(this))
  server.addEventListener("fb", classOf[String], new feedback(this))

  server.start()

  def queueJSON(): String = {
    val queue: List[StudentInQueue] = database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

  def fbJSON(): String = {
    val queue: List[String] = database.getfbQueue
    val queueJSON: List[JsValue] = queue.map((entry: String) => Json.toJson(entry))
    Json.stringify(Json.toJson(queueJSON))
  }

}

object OfficeHoursServer {
  def main(args: Array[String]): Unit = {
    new OfficeHoursServer()
  }
}

class LeaveQueueListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, username: Nothing, ackRequest: AckRequest): Unit = {
    if(server.socketToUsername.contains(socket)){
      server.database.removeStudentFromQueue(server.socketToUsername(socket))
      val usern: String = server.socketToUsername(socket)
      server.usernameToSocket = server.usernameToSocket.-(server.socketToUsername(socket))
      server.socketToUsername = server.socketToUsername.-(socket)
      socket.sendEvent("leave", usern)
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }else {
      socket.sendEvent("failedLeave")
    }
  }
}

class DisconnectionListener(server: OfficeHoursServer) extends DisconnectListener {
  override def onDisconnect(socket: SocketIOClient): Unit = {
    if (server.socketToUsername.contains(socket)) {
      server.database.removeStudentFromQueue(server.socketToUsername(socket))
      val username = server.socketToUsername(socket)
        server.socketToUsername -= socket
      if (server.usernameToSocket.contains(username)) {
        server.usernameToSocket -= username
        server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
      }
    }
  }
}


class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    if(server.socketToUsername.contains(socket)){
      socket.sendEvent("failedEntry")
    }else {
      println("enter working")
      socket.sendEvent("hide")
      server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
      server.socketToUsername += (socket -> username)
      server.usernameToSocket += (username -> socket)
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("show")
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}

class feedback(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, message: String, ackRequest: AckRequest): Unit = {
    if(server.socketToUsername.contains(socket)) {
      server.database.addFeedback(message)
      server.server.getBroadcastOperations.sendEvent("fbQueue", server.fbJSON())
    }else{
      socket.sendEvent("failedSubmit")
    }
  }
}


