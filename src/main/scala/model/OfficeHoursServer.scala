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

  val password: String = "JesseIsTheBestProfessor"

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))

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

  def formatUsername(username: String): String = {
    if(username.trim != "") {
      val onlyOneSpace: String = username.replaceAll("( +)"," ").trim()
      val splitUsername: Array[String] = onlyOneSpace.split(" ")
      var formattedUserName: String = ""
      for (string <- splitUsername) {
        if (string != splitUsername.last) {
          formattedUserName += (string.head.toUpper + string.tail.toLowerCase + " ")
        }
        else {
          formattedUserName += (string.head.toUpper + string.tail.toLowerCase)
        }
      }
      formattedUserName
    } else {
      ""
    }
  }

  def errorHandling(InQueue: Boolean, username: String, password: String, socket: SocketIOClient): Unit = {
    if(InQueue){
      socket.sendEvent("message", "You're Already In The Queue, Wait Your Turn!")
    } else if (username == ""){
      socket.sendEvent("message", "Invalid Username, Please Select Another One!")
    } else if (password != server.password) {
      socket.sendEvent("message", "Wrong Password, Please See A TA For The Password")
    } else {
      socket.sendEvent("message", "There Was A Error Entering You Into The Queue, Please Try Again!")
    }
  }

  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    val userNameAndPassword: Array[String] = username.split("#")
    val formattedUserName: String = formatUsername(userNameAndPassword.head)
    if (!server.database.inQueue(formattedUserName) && formattedUserName != "" && userNameAndPassword.last.trim() == server.password) {
      server.database.addStudentToQueue(StudentInQueue(formattedUserName, System.nanoTime()))
      server.socketToUsername += (socket -> formattedUserName)
      server.usernameToSocket += (formattedUserName -> socket)
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
      server.usernameToSocket(formattedUserName).sendEvent("message", formattedUserName + ", you have been added to the queue!")
    } else {
      errorHandling(server.database.inQueue(formattedUserName), formattedUserName, userNameAndPassword.last.trim(), socket)
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


