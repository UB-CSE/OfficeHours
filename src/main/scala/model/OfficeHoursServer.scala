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
  var taList: List[String] = List() //If registered TA, it will show a different pathway.
  var inFrontOfU: Map[String, Int] = Map() //Tells how many student is in front of you

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
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    if (!server.taList.contains(username)) {
      server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
      if (server.database.getQueue.length == 1) {
        server.usernameToSocket(username).sendEvent("message", "You are first in line")
      } else {
        server.usernameToSocket(username).sendEvent("message", (server.database.getQueue.length - 1).toString +
          " students are waiting in front of you (Estimate waiting: " + (2 * (server.database.getQueue.length - 1)).toString + ")")
        server.inFrontOfU += (username -> (server.database.getQueue.length - 1))
      }
    } else {
      socket.sendEvent("ta")
    }
    for (ta <- server.taList) {
      server.usernameToSocket(ta).sendEvent("queue", server.queueJSON())
    }
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      for (student <- server.inFrontOfU.keys) {
        server.inFrontOfU += (student -> (server.inFrontOfU(student) - 1))
        if (server.inFrontOfU(student) == 0) {
          server.usernameToSocket(student).sendEvent("message", "You are up next")
        } else {
          server.usernameToSocket(student).sendEvent("message", server.inFrontOfU(student).toString +
            " students are waiting in front of you (Estimate waiting: " + (2 * server.inFrontOfU(student)).toString + ")")
        }
      }
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
        server.inFrontOfU -= studentToHelp.username
      }
      for (ta <- server.taList) {
        server.usernameToSocket(ta).sendEvent("queue", server.queueJSON())
      }
    }
  }
}


