package model

import com.corundumstudio.socketio.listener.{DataListener, DisconnectListener}
import com.corundumstudio.socketio.{AckRequest, Configuration, SocketIOClient, SocketIOServer}
import model.database.{Database, DatabaseAPI, TestingDatabase}
import play.api.libs.json.{JsValue, Json}
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ListBuffer

class OfficeHoursServer() {

  val database: DatabaseAPI = if(Configuration.DEV_MODE){
    new TestingDatabase
  }else{
    new Database
  }

  var usernameToSocket: Map[String, SocketIOClient] = Map()
  var socketToUsername: Map[SocketIOClient, String] = Map()
  var StusernameToSocket: Map[String, SocketIOClient] = Map()//student
  var StsocketToUsername: Map[SocketIOClient, String] = Map()//student
  //var TausernameToSocket: Map[String, SocketIOClient] = Map()//TA
  //var TasocketToUsername: Map[SocketIOClient, String] = Map()//TA
  var TAList:scala.collection.mutable.ListBuffer[SocketIOClient]=scala.collection.mutable.ListBuffer()
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

  //var position:Int=0
  //var map:Map[String,Int]=Map()

  def queueJSONstudent(username:String): String = {
    //var queue=Map()
    //var stu:StudentInQueue=new StudentInQueue("",0)
    var location=0
    val queue: List[StudentInQueue] = database.getQueue.sortBy(_.timestamp)
    var i=1
    for(a<-queue) {
      if(a.username == username){
        //stu=new StudentInQueue(a.username,i)
        location=i
      }
      i+=1

    }
    location//return
    //val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())

    //position+=1
    //map+=username->position
    //val queue: List[StudentInQueue] = database.getQueue
    //queue += (student.username,position)
    //queue.toList
    //val queue:Int=position
    //val queueJSON: JsValue = Json.toJson(map(username))//queue.map((entry: Int) => entry.asJsValue())
    Json.stringify(Json.toJson(location))
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
    if (server.StsocketToUsername.contains(socket)) {
      val username = server.StsocketToUsername(socket)
      server.StsocketToUsername -= socket
      if (server.StusernameToSocket.contains(username)) {
        server.StusernameToSocket -= username
      }
    }
    if (server.TAList.contains(socket)) {
      //val username = server.TasocketToUsername(socket)
      server.TAList -= socket
      //if (server.TausernameToSocket.contains(username)) {
      //server.TausernameToSocket -= username
      //}
    }
  }
}


class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime()))
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    server.StsocketToUsername += (socket -> username)//add student
    server.StusernameToSocket += (username -> socket)//add student
    //val socketlist:List[SocketIOClient]=server.usernameToSocket.values.toList
    //for(i<-socketlist) {
    socket.sendEvent("message", "You are in the  queue at "+ server.queueJSONstudent(username).toString )
    //}

  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {
    val queue = server.database.getQueue.sortBy(_.timestamp)
    //server.socketToUsername += (socket -> username)
    server.TAList += socket
    if(queue.nonEmpty){
      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)
      socket.sendEvent("message", "You are now helping " + studentToHelp.username)
      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message", "A TA is ready to help you")
      }
      //Send message to all student about their own location in queue
      for(i<-server.StusernameToSocket.keys ) {
        if (i != studentToHelp.username) {//student getting help dont receive message
          server.StusernameToSocket(i).sendEvent("message", "You are in the  queue at " + server.queueJSONstudent(i).toString)
        }
      }
      //Send queue to all TA
      for(i<-server.TAList ) {
        i.sendEvent("queue", server.queueJSON())
      }
    }
  }
}


