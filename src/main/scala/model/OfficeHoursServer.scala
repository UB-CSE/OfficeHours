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

  //distinguish between students and TAs using data structures, so I can let them see the whole queue/their position.
  var TAsocketList: List[SocketIOClient] = List()
  var StudentSocketList: List[SocketIOClient] = List()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))

  //listen for the TA code and check
  server.addEventListener("validate", classOf[String], new Validation(this))

  //if you have a valid TA code, your socket will be added to the TA list
  server.addEventListener("verified", classOf[Nothing], new AddTA(this))

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

    //check if the student socket list already contained that socket, this is to avoid a single student enter queue repeatly
    //also, all of the usernames created under the same socket will also be ignored
    //however, this functionality will be limited because one single student could create many websites which means many web clients.

    if(!server.StudentSocketList.contains(socket)) {
      server.StudentSocketList = server.StudentSocketList :+ socket
      server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
    }
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)

    //display the whole updated queue to all of the TAs
    for(ta <- server.TAsocketList) {
      ta.sendEvent("queue", server.queueJSON())
    }

    //display the each student's position, students can't have access to see the whole queue except their position
    socket.sendEvent("position", "You" + "(" + username + ")" + "are now in position" + server.StudentSocketList.indexOf(socket).toString)
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

      //display the updated queue to all the TAs again
      for(ta <- server.TAsocketList) {
        ta.sendEvent("queue", server.queueJSON())
      }

      //avoid head of empty list error, and display each student's updated position
      if(server.StudentSocketList.nonEmpty) {
        val head = server.StudentSocketList.head
        head.sendEvent("position", "It's your position now!")

        //remove the head of the queue
        server.StudentSocketList = server.StudentSocketList.tail
        for (student <- server.StudentSocketList) {
          student.sendEvent("position", "You" + "(" + server.socketToUsername(student) + ")" + "are now in position" + server.StudentSocketList.indexOf(student).toString)
        }
      }
    }
  }
}

//if the code is 20001228, TA verified, else not
class Validation(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, code: String, ackRequest: AckRequest): Unit = {
    if(code == "20001228"){
      socket.sendEvent("boolean", "true")
    }
    else{
      socket.sendEvent("boolean", "false")
    }
  }
}

//verified TA sockets will be added to the TA list.
class AddTA(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, meaningless: Nothing, ackRequest: AckRequest): Unit = {
    server.TAsocketList = server.TAsocketList :+ socket
    socket.sendEvent("queue", server.queueJSON())
  }
}


