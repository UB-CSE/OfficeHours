package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.concurrent.duration

//noinspection DuplicatedCode
class OfficeHoursServer() {

  val database: DatabaseAPI = if (Settings.DEV_MODE) {
    new TestingDatabase
  } else {
    new Database
  }

  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()

  var TA_usernameToSocket: Map[String, SocketIOClient] = Map()
  var TA_socketToUsername: Map[SocketIOClient, String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("ta_login", classOf[String], new TALogin(this))
  server.addEventListener("jumbotron", classOf[Nothing], new JumbotronListener(this))

  server.start()

  def queueJSON_Student(sock: SocketIOClient): String = {
    if(this.socketToUsername.contains(sock)) {
      val queue: List[StudentInQueue] = database.getStudent_queue(this.socketToUsername.apply(sock))
      val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
      Json.stringify(Json.toJson(queueJSON))
    }else{
      "user no connected"
    }
  }

  def queueJSON_TA(): String = {
    val queue: List[StudentInQueue] = this.database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

}

case class UpdateJumbotron()

class JumbotronUpdater(server: OfficeHoursServer) extends Actor {
  override def receive: Receive = {
    case UpdateJumbotron =>
      for (client <- JumbotronData.clients) {
        client.sendEvent("jumbotron", server.queueJSON_Student(client))
      }
  }
}

object OfficeHoursServer {
  def main(args: Array[String]): Unit = {
    val officeHoursServer = new OfficeHoursServer()
    val actorSystem: ActorSystem = ActorSystem() // For concurrently updating the jumbotrons
    import actorSystem.dispatcher
    import scala.concurrent.duration._
    val jumbotron: ActorRef = actorSystem.actorOf(Props(classOf[JumbotronUpdater], officeHoursServer))
    actorSystem.scheduler.schedule(FiniteDuration(0, duration.MILLISECONDS), FiniteDuration(100, duration.MILLISECONDS), jumbotron, UpdateJumbotron)
  }
}

class JumbotronListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(client: SocketIOClient, data: Nothing, ackSender: AckRequest): Unit = {
    JumbotronData.clients ::= client
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

//noinspection DuplicatedCode
class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {

  override def onData(socket: SocketIOClient, jsonData: String, ackRequest: AckRequest): Unit = {
    if (!server.TA_socketToUsername.contains(socket) && !server.socketToUsername.contains(socket)) {

      val parsedJson: JsValue = Json.parse(jsonData)
      val username: String = (parsedJson \ "name").as[String]
      val helpDescription: String = (parsedJson \ "helpDescription").as[String]

      server.database.addStudentToQueue(StudentInQueue(username, helpDescription, System.nanoTime()))
      server.socketToUsername += (socket -> username)
      server.usernameToSocket += (username -> socket)

      for (i <- server.socketToUsername.keys) {
        i.sendEvent("queue", server.queueJSON_Student(i))
      }
      for (i <- server.TA_socketToUsername.keys) {
        i.sendEvent("queue", server.queueJSON_TA())
      }

    }else{
      socket.sendEvent("changeUsername","This username has been used by another student in the queue, please use another username.")
    }
  }
}


//noinspection DuplicatedCode
class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)

    if (queue.nonEmpty && server.TA_socketToUsername.contains(socket)) {
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if (server.usernameToSocket.contains(studentToHelp.username)) {
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      for (i <- server.socketToUsername.keys) i.sendEvent("queue", server.queueJSON_Student(i))
      for (i <- server.TA_socketToUsername.keys) i.sendEvent("queue", server.queueJSON_TA())
    }
  }
}

class TALogin(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socketIOClient: SocketIOClient, t: String, ackRequest: AckRequest): Unit = {
    val parsed = Json.parse(t)
    val TA_username = (parsed \ "username").as[String]

    if (!server.TA_usernameToSocket.contains(TA_username) && server.database.checkTACredentials(t)) {
      server.TA_usernameToSocket += (TA_username -> socketIOClient)
      server.TA_socketToUsername += (socketIOClient -> TA_username)
      socketIOClient.sendEvent("queue", server.queueJSON_TA())
      socketIOClient.sendEvent("release_help_button", "")
      socketIOClient.sendEvent("login_successful", "")
    }
    else if (server.TA_usernameToSocket.contains(TA_username) && server.database.checkTACredentials(t)) socketIOClient.sendEvent("already_logged_in", "")
    else socketIOClient.sendEvent("login_failed", "")
  }
}
