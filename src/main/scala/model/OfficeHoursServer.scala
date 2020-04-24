package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import play.api.libs.json.{JsValue, Json}
import model.database._


class OfficeHoursServer() {
  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }


  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()

  val config: Configuration = new Configuration {
    setHostname("localhost")
    setPort(8081)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))

  server.start()

  def queueJSON(): String = {
    val queue: List[StudentInQueue] = database.getQueue()
    val queueJSON: List[JsValue] = queue.map(_.asJsValue())
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
      server.database.removeStudentFromQueue(username)
    }
  }
}


class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, data: String, ackRequest: AckRequest): Unit = {
    val studentToAdd: Tuple2[String,String] = StudentInQueue.convertToStudent(data)
    val username: String = studentToAdd._1
    val issueTxt: String = studentToAdd._2
    //parse the data into JSON, then add that data as a new StudentInQueue to the database
    server.database.addStudentToQueue(new StudentInQueue(username, System.nanoTime().toDouble/1000000000,issueTxt))
    val queue = server.database.getQueue()
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    //loop through every student and update their position in the queue as well as queue length
    for(student <- queue){
      if(server.usernameToSocket.contains(student.username)){
        server.usernameToSocket(student.username).sendEvent("queuePos", "There is/are " + student.positionInQueue + " student(s) ahead of you, and the queue size is: " + queue.length + " student(s) long")
      }
    }
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    var queue = server.database.getQueue()
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      queue = server.database.getQueue()
      //updates queue so that changes are reflected when the total queue number is sent
      //sends the conformation message to the TA and the student
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      socket.sendEvent("issue", studentToHelp.username + "'s " + "issue is: " + studentToHelp.issue)
      socket.sendEvent("queuePos", "The queue size is: " + queue.length + " student(s) long")
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you, " + studentToHelp.username)
        server.usernameToSocket(studentToHelp.username).sendEvent("issue", "Your Issue is: " + studentToHelp.issue)
        server.usernameToSocket(studentToHelp.username).sendEvent("queuePos", "You are being helped right now.")
        server.socketToUsername -= server.usernameToSocket(studentToHelp.username)
        server.usernameToSocket -= studentToHelp.username
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
      //update position of students now that the first student has been removed
      for(student <- queue){
        if(server.usernameToSocket.contains(student.username)){
          server.usernameToSocket(student.username).sendEvent("queuePos", "There is/are " + student.positionInQueue + " student(s) ahead of you, and the queue size is: " + queue.length + " student(s) long")
        }
      }
    }
  }
}


