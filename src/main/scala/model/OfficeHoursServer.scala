package model

import java.io.{BufferedWriter, File, FileWriter}

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}

import scala.io.Source


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
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      if (new java.io.File("StudentsWhoHaveBeenOfficeHours.json").exists){
        val json = Source.fromFile("StudentsWhoHaveBeenOfficeHours.json").mkString
        val parsed: JsValue = Json.parse(json)
        val data:Map[String,Long]= parsed.as[Map[String, Long]]
        val dataTwo:Map[String,Long] = data + (studentToHelp.username -> studentToHelp.timestamp)
        val filename:String = "StudentsWhoHaveBeenOfficeHours.json"
        val file = new File(filename)
        val bw = new BufferedWriter(new FileWriter(file))
        bw.write(Json.stringify(Json.toJson(dataTwo)))
        bw.close()
      }
      else{
        val filename:String = "StudentsWhoHaveBeenOfficeHours.json"
        val file = new File(filename)
        val bw = new BufferedWriter(new FileWriter(file))
        val dataTwo:Map[String,Long] = Map(studentToHelp.username -> studentToHelp.timestamp)
        bw.write(Json.stringify(Json.toJson(dataTwo)))
        bw.close()
      }
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you!")
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}


