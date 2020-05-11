package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}
import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

class OfficeHoursServer() {

// got this from program creek

  val dateFmt = "yyyy-MM-dd"
  def today(): String = {
    val date = new Date
    val sdf = new SimpleDateFormat(dateFmt)
    sdf.format(date)
  }
  // not original

  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }
  var listOfSocks: List[SocketIOClient] = List()
  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("register",classOf[String],new AttemptR(this))
  server.addEventListener("reasonGiven",classOf[String], new ReasonListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))

  server.start()

  def queueJSON(): String = {
    val queue: List[StudentInQueue] = database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }
  def visitsJson(username:String): String = {
    val reasons: List[String] = database.getReasons(username)
    println(reasons)
    Json.stringify(Json.toJson(reasons))
  }

}

object OfficeHoursServer {
  def main(args: Array[String]): Unit = {
    new OfficeHoursServer()
  }
}


class AttemptR(server: OfficeHoursServer) extends DataListener[String]{
  override def onData(socketIOClient: SocketIOClient, t: String, ackRequest: AckRequest): Unit = {
    val parsed: JsValue = Json.parse(t)
    val username = (parsed \ "username").as[String]
    val password = (parsed \ "password").as[String]
    //method to check if this thing is correct in the database
    if(server.database.checkLogin(username,password)){
      socketIOClient.sendEvent("valid")
    }else{
      socketIOClient.sendEvent("try_again")
    }
  }
}


class ReasonListener(server: OfficeHoursServer) extends DataListener[String]{
  override def onData(socket: SocketIOClient, t: String, ackRequest: AckRequest): Unit = {

    server.database.reasonHolder(server.socketToUsername(socket), server.today().toString,t)
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
    if (!server.listOfSocks.contains(socket)) {
      server.listOfSocks = server.listOfSocks :+ socket
      server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
      server.socketToUsername += (socket -> username)
      server.usernameToSocket += (username -> socket)
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
      if(server.database.isIThere(username)==1){socket.sendEvent("prevS",server.visitsJson(username))}
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
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}


