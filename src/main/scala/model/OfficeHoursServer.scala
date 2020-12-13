package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}


class OfficeHoursServer() {

  var chatLog: Map[SocketIOClient, List[String]] = Map()

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
  server.addEventListener("chat_input", classOf[String], new ChatListener(this))

  server.start()

  def queueJSON(): String = {
    val queue: List[StudentInQueue] = database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

  def printChats(): Unit = {
    if (this.chatLog.keys.isEmpty) {
      println("Empty chat log")
    } else {
      for (user <- this.chatLog.keys) {
        if (this.chatLog(user).isEmpty) {
          println(" ")
          println("Socket: " + user)
          println("_______________")
          println(" ")
        } else {
          println(" ")
          println("Socket: " + user)
          println("_______________")
          for (chatLine <- this.chatLog(user)) {
            println(chatLine)
          }
          println(" ")
        }
      }
    }
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

class ChatListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, chatMessage: String, ackRequest: AckRequest): Unit = {
    if (server.chatLog.contains(socket)) {
      val prevChats: List[String] = server.chatLog(socket)
      val currentChats: List[String] = chatMessage :: prevChats
      server.chatLog = server.chatLog + (socket -> currentChats)
    } else {
      server.chatLog = server.chatLog + (socket -> List(chatMessage))
    }




    if (server.chatLog.keys.isEmpty) {
      println("Empty chat log")
    } else {
      for (user <- server.chatLog.keys) {
        if (server.chatLog(user).isEmpty) {
          println(" ")
          println("Socket: " + user)
          println("_______________")
          println(" ")
        } else {
          println(" ")
          println("Socket: " + user)
          println("_______________")
          for (chatLine <- server.chatLog(user)) {
            println(chatLine)
          }
          println(" ")
        }
      }
    }


  }
}


