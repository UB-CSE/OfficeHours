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
  var queueSize: Int = 0

  val config: Configuration = new Configuration {
    //"0.0.0.0"
    setHostname("localhost")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("leave_queue", classOf[Nothing], new LeaveQueueListener(this))

  server.start()

  def queueJSON(): String = {
    val queue: List[StudentInQueue] = database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

  //queue position will be negative if the person has been removed from the queue, 0 is currently being seen
  def updateQueuePositions(): Unit = {
    for((student, username) <- socketToUsername) {
      val queuePosition: Int = database.getStudent(username).queuePosition
      if (queuePosition == 1) {
        student.sendEvent("position", "You are next in the Queue")
      }else if(queuePosition > 0){
        student.sendEvent("position", "Your Queue Position: " + queuePosition)
      }else{
        student.sendEvent("position", "")
      }
    }
  }

  def clearQueue(): Unit = {
    this.database.clearQueue()
    this.socketToUsername = Map()
    this.usernameToSocket = Map()
    this.queueSize = 0
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
    server.queueSize += 1
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime(), server.queueSize))
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    server.updateQueuePositions()
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    socket.sendEvent("message", "You have been added to the queue")
  }
}

class LeaveQueueListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, t: Nothing, ackRequest: AckRequest): Unit = {
    val student: String = server.socketToUsername(socket)
    val queuePosition: Int = server.database.getStudent(student).queuePosition
    server.queueSize -= 1
    server.database.removeStudentFromQueue(student)
    server.usernameToSocket -= student
    server.socketToUsername -= socket
    server.database.studentLeftUpdatePosition(queuePosition)
    socket.sendEvent("message", "You have left the queue.")
    socket.sendEvent("position", "")

    server.updateQueuePositions()
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.queueSize -= 1
      server.database.updateStudentPosition()
      server.updateQueuePositions()
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      server.database.removeStudentFromQueue(studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
        val studentSocket: SocketIOClient = server.usernameToSocket(studentToHelp.username)
        server.usernameToSocket -= studentToHelp.username
        server.socketToUsername -= studentSocket
      }

      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}


