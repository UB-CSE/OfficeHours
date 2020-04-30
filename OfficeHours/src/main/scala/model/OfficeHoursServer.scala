package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, GetPosition, MoveDown, SetPosition, StudentActor, TestingDatabase}
import play.api.libs.json.{JsValue, Json}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

case object QueueDown
case object Update
case class StudentState(state: String)


class OfficeHoursServer() extends Actor{

  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }

  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()
  var socketToActor: Map[SocketIOClient, ActorRef] = Map()
  var TASockets: List[SocketIOClient] = List()
  var queueLength = 0

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
    val queue: List[StudentInQueue] = database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

  override def receive: Receive = {
    case QueueDown =>
      println("Queueing Down")
      for(look <- socketToActor){
        look._2 ! MoveDown
        look._2 ! GetPosition
      }
      queueLength -= 1
    case Update =>
      for(look <- socketToActor){
        look._2 ! GetPosition
      }
    case received: StudentState =>
      println("server received state")
      var jsonParsed = Json.parse(received.state)
      var user: String = (jsonParsed \ "username").as[String]
      var pos: Double = (jsonParsed \ "position").as[Double]
      usernameToSocket(user).sendEvent("queuePos", received.state)
      if(pos <= 0){
        var socket = usernameToSocket(user)
        socketToActor - socket
      }
  }

}

object OfficeHoursServer {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Server")
    var newActor = system.actorOf(Props(classOf[OfficeHoursServer]))
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
    val system = ActorSystem("StudentActor")
    var newActor = system.actorOf(Props(classOf[StudentActor], username))
    server.queueLength += 1
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
    server.socketToActor += (socket -> newActor)
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    newActor ! SetPosition(server.queueLength)
    //server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    server.self ! Update
    if(server.TASockets.size > 0){
      for(look <- server.TASockets){
        look.sendEvent("queue", server.queueJSON())
      }
    }
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    server.self ! QueueDown
    if(!server.TASockets.contains(socket)){
      server.TASockets = server.TASockets :+ socket
    }
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      for(look <- server.TASockets){
        look.sendEvent("queue", server.queueJSON())
      }
    }
  }
}


