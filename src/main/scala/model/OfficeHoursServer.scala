package model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

  var The_TA: String = ""
  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("check_TA", classOf[String], new CheckListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("TA_sign_out", classOf[Nothing], new SignOutListener(this))

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

class CheckListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, TA_name: String, ackRequest: AckRequest): Unit = {
    if(server.The_TA != ""){
      socket.sendEvent("tooManyTAs", TA_name)
    }else{
      server.The_TA += TA_name
      server.server.getBroadcastOperations.sendEvent("start", TA_name)
      socket.sendEvent("makeControlsAppear")
    }
  }
}

class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.database.addStudentToQueue(StudentInQueue(username, DateTimeFormatter.ofPattern("yyyy-MM-dd @ HH:mm").format(LocalDateTime.now)))
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      val TA_color: String = "<span style=\"color:#005BBB;\">" + server.The_TA + "</span>"
      val studentColor:String = "<span style=\"color:#005BBB;\">" + studentToHelp.username + "</span>"
      val currentSession: String = TA_color + " is currently helping " + studentColor
      server.server.getBroadcastOperations.sendEvent("message", currentSession)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        val yourSession: String = TA_color + " is currently helping you!"
        server.usernameToSocket(studentToHelp.username).sendEvent("message", yourSession)
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }else{
      val TA_color: String = "<span style=\"color:#005BBB;\">" + server.The_TA + "</span>"
      val emptySession: String = TA_color + "'s queue is currently empty"
      server.server.getBroadcastOperations.sendEvent("message", emptySession)
    }
  }
}

class SignOutListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val TA_color: String = "<span style=\"color:#005BBB;\">" + server.The_TA + "</span>"
    val endedSession: String = TA_color + " has ended their session"
    server.server.getBroadcastOperations.sendEvent("message", endedSession)
    server.The_TA = ""
  }
}

