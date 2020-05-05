package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener, ConnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}


class OfficeHoursServer() {

  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }

  var users: List[UniqueUser] = List()
//  var usernameToSocket: Map[String, SocketIOClient] = Map()
//  var socketToUsername: Map[SocketIOClient, String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  //todo Change password for TA's here! (Obviously one shouldn't store a password here when deploying)!
  val passwordTA: String = "apple"

  val server: SocketIOServer = new SocketIOServer(config)

  //Adding functionality to distinguish between TA and Student!
  //Adding a Connect Listener which will prompt the User to select if they are a TA or a Student.
  //If they are a student, they can only see THEIR request to be helped.
  //If they are a TA, enter a password (for now will just check a string), and then they can see the whole queue.

//  server.addConnectListener(new ConnectionListener(this))

  server.addDisconnectListener(new DisconnectionListener(this))
//  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))

  server.addEventListener("student", classOf[String], new Student(this))
  server.addEventListener("ta", classOf[String], new TA(this))
  server.addEventListener("password_check", classOf[String], new PasswordCheck(this))


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

//New -- might not need
//class ConnectionListener(server: OfficeHoursServer) extends ConnectListener {
//  override def onConnect(socketIOClient: SocketIOClient): Unit = {
//    //send a message to the Client to ONLY DISPLAY THE TWO BUTTONS !!
//  }
//}

class Student(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.users = new UniqueUser(username, socket, false) :: server.users
    //Send them the queue/enter them right away
    //Got rid of "EnterQueueListener"
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}

class TA(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.users = new UniqueUser(username, socket, false) :: server.users
    //Don't send the queue until they've been verified as a TA
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}

class PasswordCheck(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, input: String, ackRequest: AckRequest): Unit = {
    if (input==server.passwordTA){
      for (i <- server.users){
        if (i.getSocket==socket){
          i.ta = true
          // Send the Client a response to setup the HTML correctly!
          //server.server.getBroadcastOperations.sendEvent("verified")

        }
      }
    }
  }
}

//Used a similar data structure in my Clicker (took out the Actor references)
class UniqueUser(username: String, socket: SocketIOClient, var ta: Boolean){
  def getUsername: String ={
    this.username
  }
  def getSocket: SocketIOClient ={
    this.socket
  }
  def getTa: Boolean ={
    this.ta
  }
}


class DisconnectionListener(server: OfficeHoursServer) extends DisconnectListener {
  override def onDisconnect(socket: SocketIOClient): Unit = {
    server.users = server.users.filter(_.getSocket != socket) //Effectively 'removes' the user who disconnected
//    if (server.socketToUsername.contains(socket)) {
//      val username = server.socketToUsername(socket)
//        server.socketToUsername -= socket
//      if (server.usernameToSocket.contains(username)) {
//        server.usernameToSocket -= username
//      }
//    }
  }
}

//class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
//  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
//    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
//    server.socketToUsername += (socket -> username)
//    server.usernameToSocket += (username -> socket)
//    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
//  }
//}

class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    //first need to check if the socket is a TA!
    for (i <- server.users) {
      if (i.getSocket == socket && i.getTa) {

        val queue = server.database.getQueue.sortBy(_.timestamp)
        if (queue.nonEmpty) {
          val studentToHelp = queue.head
          server.database.removeStudentFromQueue(studentToHelp.username)

          for (loop <- server.users) {
            if (loop.getUsername == studentToHelp.username) {
              loop.getSocket.sendEvent("message", i.getUsername + " is ready to help you!")
            }
          }

          socket.sendEvent("message", "You are now helping " + studentToHelp.username + "!")
          //socket.sendEvent("message", taName + " is now helping you")
          //          if (server.usernameToSocket.contains(studentToHelp.username)) {
          //            server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
          //          }

          //Then we should resend the queue to ALL users
          //          for (loop <- server.users){
          //            loop.getSocket.
          //          }
          server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
        }
      }
    }
  }
}


