package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
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

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("send_email", classOf[String], new SendEmailListener(this))

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

class SendEmailListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, email: String, ackRequest: AckRequest): Unit = {
    val httpPost = new HttpPost("https://queueemail-0b1b.restdb.io/mail")
    httpPost.addHeader("Host","queueemail-0b1b.restdb.io")
    httpPost.addHeader("Content-Type","application/json")
    httpPost.addHeader("x-apikey","b16bab6cf671797efa788f7a9af06d4c4c6ee")
    httpPost.addHeader("Cache-Control","no-cache")

    val JSONEmail = Map[String, JsValue](
      "to"-> Json.toJson(email),
      "subject"-> Json.toJson("Your TA is ready for you"),
      "html"-> Json.toJson("<html><body><h3>  A TA IS READY FOR YOUR APPOINTMENT</h3></body></html>"),
      "sendername"-> Json.toJson("Queue Bot")
    )
    val  emailEntity = new StringEntity(Json.stringify(Json.toJson(JSONEmail)), ContentType.create("application/json", "UTF-8"))
    httpPost.setEntity(emailEntity)
    val httpclient = HttpClients.createDefault
    val response = httpclient.execute(httpPost)
    val stringResp = response.getEntity
    response.close()
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

      socket.sendEvent("emailAlert", server.socketToUsername(socket))
      socket.sendEvent("message", "You are now helping " + server.socketToUsername(socket))

      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      server.server.getBroadcastOperations.sendEvent("queue", server.queueJSON())
    }
  }
}


