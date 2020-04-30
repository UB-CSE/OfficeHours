package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}
import java.util.{Calendar,TimeZone}


class OfficeHoursServer() {

  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }

  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()

  var taList:List[String] = List()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("ta_login",classOf[String], new TALoginListener(this))
  server.addEventListener("ta_logout", classOf[String], new TALogoutListener(this))
  server.addEventListener("refresh",classOf[Nothing],new RefreshListener(this))

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
    server.database.addStudentToQueue(StudentInQueue(username, Calendar.getInstance(TimeZone.getTimeZone("EST")).getTime.toString))
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
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}

class TALoginListener(server: OfficeHoursServer) extends DataListener[String]{
  override def onData(socketIOClient: SocketIOClient, t: String, ackRequest: AckRequest): Unit = {
    server.taList = server.taList :+ t
    server.server.getBroadcastOperations.sendEvent("talog", Json.stringify(Json.toJson(server.taList)))
  }

}
class TALogoutListener(server: OfficeHoursServer) extends DataListener[String]{
  override def onData(socketIOClient: SocketIOClient, t: String, ackRequest: AckRequest): Unit = {
    if(server.taList.contains(t)){
      server.taList = server.taList.filter(_!=t)
      server.server.getBroadcastOperations.sendEvent("talog", Json.stringify(Json.toJson(server.taList)))
    }
  }
}

class RefreshListener(server: OfficeHoursServer) extends  DataListener[Nothing]{
  override def onData(socketIOClient: SocketIOClient, t: Nothing, ackRequest: AckRequest): Unit = {
    server.server.getBroadcastOperations.sendEvent("talog", Json.stringify(Json.toJson(server.taList)))
  }
}


