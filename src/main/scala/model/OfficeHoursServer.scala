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

  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  //Connection
  server.addConnectListener(new ConnectionListener())

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))

  //Get a login or register information as Json String
  server.addEventListener("login", classOf[String], new Login(this))
  server.addEventListener("register", classOf[String], new Register(this))

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

//Connection:
class ConnectionListener() extends ConnectListener {
  override def onConnect(socket : SocketIOClient) : Unit = {
    println("Connected: " + socket)
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
    //Save the user into the database
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    //This to sort a student by time
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}

/**
 * Login and register classes to get a user data:
 * Compare to the Database
 * Get a data related to a user that logged in
 * Check if user already exists by checking its FullName and Username and Email into the DB
 *
 * */
class Login(server : OfficeHoursServer) extends DataListener[String]{
  override def onData(client: SocketIOClient, data: String, ackSender: AckRequest): Unit = {
    println("Welcome: " + data)
    //Send to a specific user who clicked login
    server.server.getBroadcastOperations.sendEvent("loggedIn", data)
  }
}

class Register(server: OfficeHoursServer) extends DataListener[String]{
  override def onData(socket: SocketIOClient, data: String, ackSender: AckRequest): Unit = {
    //Add the user into the db || Data will become username
    server.database.addStudentToQueue(StudentInQueue(data, System.nanoTime()))
    server.socketToUsername += (socket -> data)
    server.usernameToSocket += (data -> socket)
    println("Welcome: " + data)
    //Send to a specific user who clicked register
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}

