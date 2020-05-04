package model

import com.corundumstudio.socketio.listener.{ConnectListener, DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}
import com.github.t3hnar.bcrypt._

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


  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  //listens for event register and adds student and password to database // expects json in format {username: "username", password: "password"}
  server.addEventListener("register", classOf[String], new Register(this))
  //listens for event login and attempts to login in student // expects json in format {username : "username", password: "password"}
  server.addEventListener("login", classOf[String], new Login(this))
  //challenge to determine if the user is already logged in or not
  server.addEventListener("challenge", classOf[String], new Challenge(this))
  //reconnect user after reloading page
  server.addEventListener("reconnect", classOf[String], new Challenge(this))

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

class Challenge(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(client: SocketIOClient, data: String, ackSender: AckRequest): Unit = {
    val json = Json.parse(data)
    val username = (json \ "username").as[String]

    if(username == "null") {
      client.sendEvent("invalid")
      println(username)
    } else if(server.socketToUsername.contains(client)) {
      println("here")
      val username1 = server.socketToUsername(client)
      println(username)
      println(username1)
      if(username1 == username) {
        client.sendEvent("valid")
      } else {
        client.sendEvent("invalid")
      }
    } else {
      println(server.socketToUsername.contains(client))
      println(server.socketToUsername)
      println(client)
      client.sendEvent("invalid")
    }
  }
}

class Login(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(client: SocketIOClient, data: String, ackSender: AckRequest): Unit = {
    val json = Json.parse(data)
    //parses expected json
    val username = (json \ "username").as[String]
    val password = (json \ "password").as[String]
    //checks if password is correct and sends back if the password is invalid or other error messages
    var login: String = ""
    if (password != "") {
      login = server.database.authenticate(username, password)
    }
    if(password == ""){
      client.sendEvent("error")
    }
    else if(username == "null"){
      client.sendEvent("bad user")
    }
    else if (login == "logged in") {
      // sends back event if login was successful
      client.sendEvent("successful login", username)
      //add actor creation here if you would like for concurrency
      //adds username and socket to maps
      server.usernameToSocket += (username -> client)
      server.socketToUsername += (client -> username)
      println(server.usernameToSocket)
      println(server.socketToUsername)
    }
    else if ("bad pass" == login) {
      //if password is invalid/wrong this is sent back to client
      client.sendEvent("bad pass")
    }
    else if ("DNE" == login) {
      //send back event DNE if account does not exist
      client.sendEvent("DNE")
    }
    else if (login == "bad user"){
      client.sendEvent("bad user")
    }
    else {
      //all other errors fall in this category and send back an error message to the client
      client.sendEvent("error")
    }
  }
}

class Register(server: OfficeHoursServer) extends DataListener[String]{
  override def onData(client: SocketIOClient, data: String, ackSender: AckRequest): Unit = {
    val json = Json.parse(data)
    //parses expected json
    val password = (json \ "password").as[String]
    val username = (json \ "username").as[String]
    //creates salt for password
    val salt = generateSalt
    // hashes password using salt
    val hashpass = password.bcrypt(salt)
    //checks if that user can be added to data base or if it already exists
    var check: Boolean = false
    if(username != "null") {
      check = server.database.addUserToAuthenticate(username, hashpass, salt)
    }
    if(username == "null"){
      client.sendEvent("error")
    }
    if(check) {
      //if it was created successfully event is sent back to like
      client.sendEvent("successCreate")
    }
    else {
      //if unsuccessful creation occurs this event is sent back to client
      client.sendEvent("failedCreate")
    }
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
    if(server.usernameToSocket.contains(username) && server.socketToUsername.contains(socket)) {
      if(server.usernameToSocket(username) == socket && server.socketToUsername(socket) == username) {
        server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
        server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
      }
    }
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
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}


