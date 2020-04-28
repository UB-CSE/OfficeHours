package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}


//noinspection DuplicatedCode
class OfficeHoursServer() {

  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }

  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()

  var TA_usernameToSocket: Map[String, SocketIOClient] = Map()
  var TA_socketToUsername: Map[SocketIOClient, String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("ta_login", classOf[String], new TALogin(this))

  server.start()

  def queueJSON_Student(sock: SocketIOClient): String = {
    val queue: List[StudentInQueue] = database.sendQueueStudent(this.socketToUsername.apply(sock))
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

  def queueJSON_TA(): String = {
    val queue: List[StudentInQueue] = this.database.getQueue
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

//noinspection DuplicatedCode
class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    for (i <- server.socketToUsername.keys) i.sendEvent("queue", server.queueJSON_Student(i))
    for (i <- server.TA_socketToUsername.keys) i.sendEvent("queue", server.queueJSON_TA())
  }
}


//noinspection DuplicatedCode
class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      for (i <- server.socketToUsername.keys) i.sendEvent("queue", server.queueJSON_Student(i))
      for (i <- server.TA_socketToUsername.keys) i.sendEvent("queue", server.queueJSON_TA())
    }
  }
}

class TALogin(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socketIOClient: SocketIOClient, t: String, ackRequest: AckRequest): Unit = {
    if (server.database.checkTACredentials(t)) {
      val parsed = Json.parse(t)
      val TA_username = (parsed \ "username").as[String]
      server.TA_usernameToSocket += (TA_username -> socketIOClient)
      server.TA_socketToUsername += (socketIOClient -> TA_username)
      socketIOClient.sendEvent("queue", server.queueJSON_TA())
      socketIOClient.sendEvent("release_help_button", "")
    }
  }
}


