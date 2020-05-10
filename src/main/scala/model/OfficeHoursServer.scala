package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}
import java.util.Calendar


class OfficeHoursServer() {


  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }

  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()
  var staffToSocket: Map[String, SocketIOClient] = Map()
  var socketToStaff: Map[SocketIOClient,String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("authenticate_TA", classOf[String], new AuthenticationListener(this))
  server.addEventListener("register_TA", classOf[String], new RegisterStaffListener(this))

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

class AuthenticationListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, data: String, ackRequest: AckRequest): Unit = {
    val staffStore: List[Staff] = server.database.getStaff
    val ubit: String = data.split(":")(0)
    //println("Ubit:" + ubit)
    val password: String = data.split(":")(1)
    //println("password:" + password)
    for (staff <- staffStore){
      //println("ubit:" + staff.ubit +"||password:" + staff.password)
      if (staff.ubit == ubit && staff.password == password){
        socket.sendEvent("authenticateStaff", ubit)
      }
    }
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}

class RegisterStaffListener(server: OfficeHoursServer) extends  DataListener[String] {
  override def onData(socket: SocketIOClient, data: String, ackRequest: AckRequest): Unit = {
    val ubit: String = data.split(":")(0)
    val password: String = data.split(":")(1)
    server.database.addStaff(new Staff(ubit,password))
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
    if (server.socketToStaff.contains(socket)){
      val ubit = server.socketToStaff(socket)
        server.socketToStaff -= socket
      if(server.staffToSocket.contains(ubit)){
        server.staffToSocket -= ubit
      }
    }
  }
}


class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  val time: Calendar = Calendar.getInstance()
  val minutes: Int = time.get(Calendar.MINUTE)
  var hours: Int = time.get(Calendar.HOUR_OF_DAY)
  var currentHour: Int = _
  var stamp: String = "AM"
  if (hours > 12){
    currentHour = hours % 12
    stamp = "PM"
  }
  val retString: String = currentHour.toString + ":" + minutes.toString + " " + stamp
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.database.addStudentToQueue(StudentInQueue(username, retString))
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


