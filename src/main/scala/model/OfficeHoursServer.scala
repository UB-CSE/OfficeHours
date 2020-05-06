package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import main.scala.model.HelperFunctions.FileReader
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}


class OfficeHoursServer() {

  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }

  /*if a TA enters their name the queue will be opened to students
  This will allow the TA to open the queue when they get there so students cannot arrive early
  to get at the front of the list*/
  var queueOpen=false
  var currentTA=""

  //this is the file with all the TA "passwords"
  val filenameTA="src/main/scala/model/TAPasswords(names).txt"


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

  /*takes the TA's password (for trial purposes just their name) which they enter and if its stored in the
   file the queue will open to students. If they enter their name and press close it'll close the queue.The
   person who opens the queue does not have to close it*/
  server.addEventListener("oh_starting", classOf[String], new StartingOHListener(this))
  server.addEventListener("oh_ending", classOf[String], new EndingOHListener(this))


  /* if office hours are back to back the next person can pick up where the first person left off. If office hours
  are spread far apart, the leaving TA can clear the queue so the next TA can start fresh. TA must put in their password
  to clear so a student cannot clear it. If the TA before forgot to clear the queue the next TA can also clear it
  themselves*/
  server.addEventListener("clear_queue", classOf[String], new ClearQueueListener(this))

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
    //if the queue is open students can enter, if its not open students cannot enter
    if (server.queueOpen) {
      /*changes the username to all lowercase so that a user can't enter multiple times with the same username but
    capitals and lowercase's in different spots*/
      val user = username.toLowerCase
      //if the username is already here they won't be added to the queue again
      if (!server.usernameToSocket.contains(user))
        server.database.addStudentToQueue(StudentInQueue(user, System.nanoTime()))
      server.socketToUsername += (socket -> user)
      server.usernameToSocket += (user -> socket)
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}

class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      val studentUsername=studentToHelp.username
      //gets rid of the username in the map so the student can hop back into the queue if needed
      server.usernameToSocket-=studentUsername
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}

class StartingOHListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, taPassword: String, ackRequest: AckRequest): Unit = {
    //if the queue isn't open a TA must open it
    if (!server.queueOpen){
      /*uses the fileReader helper function (in the HelperFunction package) to see if the TA's
      password is valid */
      if (FileReader.fileReader(server.filenameTA,taPassword)){
        //if the password is valid the office hours will open and a message will send that the server is open
        server.queueOpen=true
        socket.sendEvent("message", "Office Hours Open")
      }
    }
  }
}

class EndingOHListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, taPassword: String, ackRequest: AckRequest): Unit = {
    //if the queue is open a TA must close it.
    // Any TA can close the Queue once its open by putting in their password
    if (server.queueOpen){
      if (FileReader.fileReader(server.filenameTA,taPassword)){
        server.queueOpen=false
        socket.sendEvent("message", "Office Hours Closed")
        for ((key, value) <- server.usernameToSocket) {
          //gets rid of the username's in the map and the queue so the students can hop back into the queue if needed
          server.database.removeStudentFromQueue(key)
          server.usernameToSocket-=key
        }
      }
    }
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}

class ClearQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, taPassword: String, ackRequest: AckRequest): Unit = {
    /*If they enter their password a TA can clear the queue. Any TA can do this.
    This can be done if the queue is open or not */
    if (FileReader.fileReader(server.filenameTA,taPassword)) {
      for ((key, value) <- server.usernameToSocket) {
        //gets rid of the username's in the map and the queue so the students can hop back into the queue if needed
        server.database.removeStudentFromQueue(key)
        server.usernameToSocket-=key
      }
    }
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}

