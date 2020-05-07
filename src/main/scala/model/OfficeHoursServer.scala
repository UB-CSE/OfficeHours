package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}

import scala.collection.mutable._

class OfficeHoursServer() {
  val enteredQueue:Queue[SocketIOClient] = new Queue()
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
    var queueMessage:String = ""
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    if (!server.enteredQueue.exists(x => {x == socket})){
      server.enteredQueue.enqueue(socket)
      queueMessage = "You (" + username + ") have entered the queue. Your position is " + (server.enteredQueue.indexOf(socket).toInt + 1)  + " in the queue."
    }
    else{
      queueMessage = "You are already in the queue, your position is " + (server.enteredQueue.indexOf(socket).toInt + 1) + "."
    }
    socket.sendEvent("queue",queueMessage)
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    if(server.enteredQueue.nonEmpty){
      var studentHelped:SocketIOClient = server.enteredQueue.dequeue()
      socket.sendEvent("message", "You are now helping " + server.socketToUsername(studentHelped))
      studentHelped.sendEvent("message","A TA is ready to help you")
    }
    else{
      socket.sendEvent("message","There is no one in the queue at the moment.")
    }
  }
}


