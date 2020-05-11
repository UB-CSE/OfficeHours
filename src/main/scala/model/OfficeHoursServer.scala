package model

import com.corundumstudio.socketio.listener.{ConnectListener, DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}


class OfficeHoursServer() {

  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }

  // inputted usernames will be checked against this list to determine if the user is a TA
  // I am deeply sorry if I forgot someone
  val TAList: List[String] = List("Rin", "Mike", "Jon", "Megan", "Lana", "Shamroy", "Jesse", "Joseph", "Taisia", "John", "Stephen", "Sayef", "Patrick", "Snigdha", "Logan", "Kevin", "Jacob", "Tanvie", "Nicholas")

  // used to send things to all TAs, students, and those who haven't entered a name yet
  var allSockets: List[SocketIOClient] = List()

  // a list of all the active TAs
  var activeTAs: List[String] = List()

  // data structures that allow you to go from socket to username and vice versa
  var studentUsernameToSocket: Map[String, SocketIOClient] = Map()
  var studentSocketToUsername: Map[SocketIOClient, String] = Map()

  var TAUsernameToSocket: Map[String, SocketIOClient] = Map()
  var TASocketToUsername: Map[SocketIOClient, String] = Map()


  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addConnectListener(new ConnectionListener(this))
  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new SubmitNameListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))

  server.start()


  def queueJSON(): String = {
    val queue: List[StudentInQueue] = database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

  // turns the active TA list into a JSON string
  def TAListJSON(): String = {
    val listJson: List[JsValue] = activeTAs.map((entry: String) => Json.toJson(entry))
    Json.stringify(Json.toJson(listJson))
  }
}


object OfficeHoursServer {
  def main(args: Array[String]): Unit = {
    new OfficeHoursServer()
  }
}

// add all users to allSockets when they connect and send them the active TA list
class ConnectionListener(server: OfficeHoursServer) extends ConnectListener {
  override def onConnect(socket: SocketIOClient): Unit = {
    server.allSockets = server.allSockets :+ socket

    // send the user the TA list
    socket.sendEvent("ta_list", server.TAListJSON())
  }
}

// removes user from data structures, if student, remove from database
class DisconnectionListener(server: OfficeHoursServer) extends DisconnectListener {
  override def onDisconnect(socket: SocketIOClient): Unit = {
   // if they're a student
    if (server.studentSocketToUsername.contains(socket)) {
      val username = server.studentSocketToUsername(socket)
        server.studentSocketToUsername -= socket
      if (server.studentUsernameToSocket.contains(username)) {
        server.studentUsernameToSocket -= username
      }
      server.allSockets = server.allSockets.filter(_ != socket)
      val position: Int = server.database.getPosition(username)
      server.database.removeStudentFromQueue(username)

      // send the other students their updated positions and update the database
      for (student <- server.studentSocketToUsername.keys) {
        val name: String = server.studentSocketToUsername(student)
        // only update students behind the one this one
        if (server.database.getPosition(name) > position && position > 0) {
          val newPosition: Int = server.database.getPosition(name) - 1
          student.sendEvent("position", "Position in queue: " + newPosition.toString)
          server.database.updatePosition(name, newPosition)
        }
      }

      // send the TAs an updated queue
      for (ta <- server.TASocketToUsername.keys) {
        ta.sendEvent("queue", server.queueJSON())
      }
    }

    // if they're a TA
    if (server.TASocketToUsername.contains(socket)) {
      val username = server.TASocketToUsername(socket)
      server.TASocketToUsername -= socket
      if (server.TAUsernameToSocket.contains(username)) {
        server.TAUsernameToSocket -= username
      }
      //remove them from the active TA list and update the list for all users
      server.activeTAs = server.activeTAs.filter(_ != username)
      for (user <- server.allSockets) {
        user.sendEvent("ta_list", server.TAListJSON())
      }
    }
  }
}


// add user to data structures
class SubmitNameListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {

    // if the user is a TA
    if (server.TAList.contains(username)) {

      // add to data structures
      server.TASocketToUsername += (socket -> username)
      server.TAUsernameToSocket += (username -> socket)

      // add to active list
      server.activeTAs = server.activeTAs :+ username

      // send the updated list of active TAs to all users
      for (user <- server.allSockets) {
        user.sendEvent("ta_list", server.TAListJSON())
      }

      // send them the queue
      socket.sendEvent("queue", server.queueJSON())

      // add the TA Ready to Help Button
      socket.sendEvent("ta_button")

      //update message
      socket.sendEvent("message", "You can now begin to help students")
    }

    // if the user is a student
    else {

      // add to database
      val lengthQueue: Int = server.database.getQueue.length + 1
      server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime(), lengthQueue))

      // add to data structures
      server.studentSocketToUsername += (socket -> username)
      server.studentUsernameToSocket += (username -> socket)

      // send the updated queue to all the TAs currently online
      for (ta <- server.TASocketToUsername.keys) {
        ta.sendEvent("queue", server.queueJSON())
      }

      // send the student their place in the queue
      socket.sendEvent("position", "Position in queue: " + lengthQueue.toString)

      // send the student the list of TAs on duty
      socket.sendEvent("ta_list", server.TAListJSON())

      // update message
      socket.sendEvent("message", "You have been placed in the queue")
    }
  }
}

class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if (queue.nonEmpty) {
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if (server.studentUsernameToSocket.contains(studentToHelp.username)) {
        server.studentUsernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }

      // send the other students their updated positions and update in database
      for (student <- server.studentSocketToUsername.keys) {

        if (server.database.getPosition(server.studentSocketToUsername(student)) > 1) {
          val username: String = server.studentSocketToUsername(student)
          val newPosition: Int = server.database.getPosition(username) - 1
          student.sendEvent("position", "Position in queue: " + newPosition.toString)
          server.database.updatePosition(username, newPosition)
        }

        // send the students being helped a unique message
        else {
          server.studentUsernameToSocket(studentToHelp.username).sendEvent("position", "You are currently being helped")
        }
      }

      // send updated queue to the TAs
      for (ta <- server.TASocketToUsername.keys) {
        ta.sendEvent("queue", server.queueJSON())
      }
    }
  }
}