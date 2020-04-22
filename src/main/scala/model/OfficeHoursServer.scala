package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import play.api.libs.json.{JsValue, Json}


class OfficeHoursServer() {

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
    val queue: List[StudentInQueue] = Database.getQueue()
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
      //if the student disconnects, remove them from the queue
      Database.removeStudentFromQueue(username)
    }
  }
}


class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, data: String, ackRequest: AckRequest): Unit = {
    val studentToAdd: Tuple2[String,String] = StudentInQueue.convertToStudent(data)
    val username: String = studentToAdd._1
    val issueTxt: String = studentToAdd._2
    Database.addStudentToQueue(StudentInQueue(username, System.nanoTime().toDouble/1000000000,issueTxt))
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    for(student <- Database.getQueue()){
      if(server.usernameToSocket.contains(student.username)){
        server.usernameToSocket(student.username).sendEvent("queuePos", "Your Position in the Queue is: " + student.positionInQueue)
      }
    }
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = Database.getQueue(true)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      Database.removeStudentFromQueue(studentToHelp.username)
      //sends the conformation message to the TA and the student
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      socket.sendEvent("issue", studentToHelp.username + "'s" + "issue is: " + studentToHelp.issue)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you, " + studentToHelp.username)
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "Your Issue is: " + studentToHelp.issue)
      }
      for(student <- queue){
        if(server.usernameToSocket.contains(student.username)){
          server.usernameToSocket(student.username).sendEvent("queuePos", "There is/are " + student.positionInQueue + "(s) ahead of you")
        }
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}


