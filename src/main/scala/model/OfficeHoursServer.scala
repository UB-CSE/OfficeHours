package model

import akka.actor._
import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}

class OfficeHoursServer() {

  val database: DatabaseAPI = if (Configuration.DEV_MODE) {
    new TestingDatabase
  } else {
    new Database
  }

  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()
  var socketToQueueActor: Map[SocketIOClient, ActorRef] = Map()
  var queueActorToSocket: Map[ActorRef, SocketIOClient] = Map()
  var tAS: List[SocketIOClient] = List()

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

  def queuePosition(username: String): String = {
    val queuePosition: Int = database.queuePosition(username)
    Json.stringify(Json.toJson(queuePosition))
  }

  def queuePositionUpdate(): String = {
    val queuePositionUpdated: Int = database.queuePositionUpdate
    Json.stringify(Json.toJson(queuePositionUpdated))
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


    server.usernameToSocket(username).sendEvent("position",server.queuePosition(username))
    //server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())//, server.queuePosition(username))
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    println(queue)
    //server.database.queuePositionUpdate
    server.tAS :+= socket

    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      for(socket <- server.socketToUsername.keys) {
        val user = server.socketToUsername(socket)
        val position = server.database.queuePosition(user)
        println(position)
        socket.sendEvent("position", position.toString)
      }
    }
    for(ta <- server.tAS){
      ta.sendEvent("queue", server.queueJSON())
    }
  }
}


