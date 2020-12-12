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
  var TAToSocket: Map[String, SocketIOClient] = Map()
  var socketToTA: Map[SocketIOClient, String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("TA_sign_in", classOf[String], new TA_EnterListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("CheckQueue", classOf[String], new CheckQueueListener(this))

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


class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)

    val queue: List[StudentInQueue] = server.database.getQueue.sortBy(_.timestamp)
    val StringQueue: List[String] = queue.map(_.username)
    var place: Int = 0
    for (name <- StringQueue) {
      if (name == username) {
        place
      } else {
        place+=1
      }
    }
    val placeInJson: JsValue = Json.toJson(place)

    socket.sendEvent("position_in_queue", Json.stringify(placeInJson))
    place = 0
    //socket.sendEvent("queue", server.queueJSON()) //need chages
  }
}

class CheckQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))

    val queue: List[StudentInQueue] = server.database.getQueue.sortBy(_.timestamp)
    val StringQueue: List[String] = queue.map(_.username)
    var place: Int = 0
    for (name <- StringQueue) {
      if (name == username) {
        place
      } else {
        place+=1
      }
    }
    val placeInJson: JsValue = Json.toJson(place)
    socket.sendEvent("position_in_queue", Json.stringify(placeInJson))
    place = 0
    //socket.sendEvent("queue", server.queueJSON()) //need chages
  }
}

class TA_EnterListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(client: SocketIOClient, username: String, ackSender: AckRequest): Unit = {
    server.TAToSocket += (username -> client)
    server.socketToTA += (client -> username)
    client.sendEvent("TA_in_progress")
  }
}

class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val TA_name = server.socketToTA(socket)
    val jsonTAname = Json.toJson(TA_name)
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    server.usernameToSocket(studentToHelp.username).sendEvent("UpdateStudent", Json.stringify(jsonTAname))
    }
  }
}


