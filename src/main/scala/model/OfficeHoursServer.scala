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

  var student_usernameToSocket: Map[String, SocketIOClient] = Map()
  var student_socketToUsername: Map[SocketIOClient, String] = Map()
  var TA_sockets : List[SocketIOClient] = List()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("enter_TA", classOf[Nothing], new TAEnterListener(this))

  server.start()

  def queueJSON(): String = {
    val queue: List[StudentInQueue] = database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

  def positionJSON(username : String) : String = {
    val position = database.getQueue.sortBy(_.timestamp).indexWhere(_.username == username)
    Json.stringify(Json.toJson(Map("position" -> Json.toJson(position + 1))))
  }
}

object OfficeHoursServer {
  def main(args: Array[String]): Unit = {
    new OfficeHoursServer()
  }
}


class DisconnectionListener(server: OfficeHoursServer) extends DisconnectListener {
  override def onDisconnect(socket: SocketIOClient): Unit = {
    //if it's a student (socket is in student_socketToUsername)
    if (server.student_socketToUsername.contains(socket)) {
      //remove that person in the maps
      val username = server.student_socketToUsername(socket)
      server.student_socketToUsername -= socket
      if (server.student_usernameToSocket.contains(username)) {
        server.student_usernameToSocket -= username
      }

      server.database.removeStudentFromQueue(username)

      //send TAs updated queue
      for(ta <- server.TA_sockets){
        ta.sendEvent("queue", server.queueJSON())
      }

      //send students in the queue their new position
      val usernameList : List[String] = server.database.getQueue.sortBy(_.timestamp).map(_.username)
      for(index <- 0 to usernameList.length - 1){
        val position = Json.stringify(Json.toJson(Map("position" -> Json.toJson(index + 1))))
        server.student_usernameToSocket(usernameList(index)).sendEvent("position", position)
      }
    }

    if(server.TA_sockets.contains(socket)){
      val index = server.TA_sockets.indexWhere(_ == socket)
      server.TA_sockets = server.TA_sockets.drop(index)
    }
  }
}


class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    //don't allow same socket to connect with two different usernames
    //don't allow two different people with the same username
    if(!server.student_socketToUsername.contains(socket) && !server.student_usernameToSocket.contains(username)) {
      socket.sendEvent("name_valid")
      server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
      server.student_socketToUsername += (socket -> username)
      server.student_usernameToSocket += (username -> socket)

      //send tas updated queue
      for(ta <- server.TA_sockets){
        ta.sendEvent("queue", server.queueJSON())
      }

      //send position in line to the person that is entering the queue
      socket.sendEvent("position", server.positionJSON(username))
    } else if(server.student_usernameToSocket.contains(username)){
      socket.sendEvent("name_already_used")
    }
  }
}

class TAEnterListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, username: Nothing, ackRequest: AckRequest): Unit = {
    server.TA_sockets ::= socket
    socket.sendEvent("queue", server.queueJSON())
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.student_usernameToSocket.contains(studentToHelp.username)){
        server.student_usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }

      for(ta <- server.TA_sockets){
        ta.sendEvent("queue", server.queueJSON())
      }

      //send students updated position in queue
      val usernameList : List[String] = server.database.getQueue.sortBy(_.timestamp).map(_.username)
      for(index <- 0 to usernameList.length - 1){
        val position = Json.stringify(Json.toJson(Map("position" -> Json.toJson(index + 1))))
        server.student_usernameToSocket(usernameList(index)).sendEvent("position", position)
      }
    }
  }
}



