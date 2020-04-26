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

  var peopleWaiting:List[String] = List()
  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()
  var taSocketToUsername:Map[SocketIOClient,String] = Map()

  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue_TA", classOf[String], new EnterQueueTAListener(this))
  server.addEventListener("enter_queue_Student", classOf[String], new EnterQueueStudentListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))

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

    if(server.taSocketToUsername.contains(socket)){
        server.taSocketToUsername -= socket
    }
  }
}


class EnterQueueTAListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    server.taSocketToUsername += (socket -> username)
    socket.sendEvent("queue", server.queueJSON())
  }
}

class EnterQueueStudentListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
    server.peopleWaiting = server.peopleWaiting :+ username
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    socket.sendEvent("position", Json.stringify(Json.toJson(server.peopleWaiting.length)))

    for(x <- server.taSocketToUsername.keys){
      x.sendEvent("queue",server.queueJSON())
    }

  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      server.peopleWaiting = server.peopleWaiting.drop(1)
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("taMessage", "You are now helping " + studentToHelp.username)

      val message:String = "TA " + server.taSocketToUsername(socket) + " is ready to help you!"
      server.usernameToSocket(studentToHelp.username).sendEvent("studentMessage", message)

      for(x <- server.taSocketToUsername.keys){
        x.sendEvent("queue",server.queueJSON())
      }


      for(i <- server.peopleWaiting){
        server.usernameToSocket(i).sendEvent("position", Json.stringify(Json.toJson(server.peopleWaiting.indexOf(i) + 1)))
      }

    }
  }
}


