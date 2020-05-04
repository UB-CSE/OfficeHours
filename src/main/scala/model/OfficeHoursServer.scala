package model

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

  var TAtoSocket: Map[String,SocketIOClient]= Map()
  var SocketToTA: Map[SocketIOClient,String]=Map()

  var loggedInClients:List[SocketIOClient]=List()

  var studentCount:Int=0
  var taCount:Int=0

  val random = new scala.util.Random

  val cred=Source.fromFile("credentials.json").mkString
  var TAonline:Map[String,Boolean]=createdTaOnline(cred)


  val config: Configuration = new Configuration {
    setHostname("0.0.0.0")
    setPort(8080)
  }

  val server: SocketIOServer = new SocketIOServer(config)

  server.addDisconnectListener(new DisconnectionListener(this))
  server.addEventListener("enter_queue", classOf[String], new EnterQueueListener(this))
  server.addEventListener("ready_for_student", classOf[Nothing], new ReadyForStudentListener(this))
  server.addEventListener("display_TA", classOf[String], new displayTAListener(this))
  server.addEventListener("alert_page", classOf[String], new alertListener(this))
  server.addEventListener("done_helping", classOf[String], new doneHelpingListener(this))
  server.addEventListener("first_count", classOf[Nothing], new countListener(this))
  server.addEventListener("check_login", classOf[String], new loginListener(this))
  server.addEventListener("get_TA_Stat", classOf[Nothing], new TAStatListener(this))
  server.addEventListener("get_Student_Stat", classOf[String], new StudentStatListener(this))




  server.start()

  def queueJSON(): String = {
    val queue: List[StudentInQueue] = database.getQueue
    val queueJSON: List[JsValue] = queue.map((entry: StudentInQueue) => entry.asJsValue())
    Json.stringify(Json.toJson(queueJSON))
  }

  def countJSON():String={
    var counter:Map[String,JsValue]=Map(
      "student"->Json.toJson(studentCount),
      "ta"->Json.toJson(taCount)
    )
    Json.stringify(Json.toJson(counter))
  }

  def removeSocket(socket: SocketIOClient,server:OfficeHoursServer): Unit ={
    server.loggedInClients=server.loggedInClients.filter(_!=socket)
  }
  def broadcastQueue(server: OfficeHoursServer,messageType:String,value:String): Unit ={
    server.loggedInClients.foreach(_.sendEvent(messageType,value))
  }
  def createdTaOnline(file:String): Map[String,Boolean]={
    var TaMap: Map[String,Boolean]=Map()
    val pasred=Json.parse(file)
    val ListofTA:Array[Map[String,String]]=(pasred\"TA_Credentials").as[Array[Map[String,String]]]

    for(elem<-ListofTA){
      val name=elem("ubit")
      val bool=elem("online").toBoolean
      TaMap+=(name->bool)
    }
    TaMap
  }
  def updateTaOnline(file:String,username:String,status:Boolean): Map[String,Boolean]={
    var TaMap: Map[String,Boolean]=Map()
    val pasred=Json.parse(file)
    val ListofTA:Array[Map[String,String]]=(pasred\"TA_Credentials").as[Array[Map[String,String]]]

    for(elem<-ListofTA){
      val name=elem("ubit")
      val bool=elem("online").toBoolean
      if(name==username){
        TaMap+=(name->status)
      }
      else{
        TaMap+=(name->bool)
      }
    }
    TaMap
  }

  def validateLogin(file:String,username:String,password:String,server:OfficeHoursServer): Boolean ={
    var check:Boolean=false
    val pasred=Json.parse(file)
    val ListofTA:Array[Map[String,String]]=(pasred\"TA_Credentials").as[Array[Map[String,String]]]

    for(elem<-ListofTA){
      val name=elem("ubit")
      if( elem("ubit")==username && elem("password")== password && !server.TAonline(name) ){
        check=true
        server.TAonline=server.updateTaOnline(file,name,true)
      }
    }
    check
  }

}

object OfficeHoursServer {
  def main(args: Array[String]): Unit = {
    new OfficeHoursServer()
  }
}

class DisconnectionListener(server: OfficeHoursServer) extends DisconnectListener {
  override def onDisconnect(socket: SocketIOClient): Unit = {

    server.removeSocket(socket,server)

    if (server.socketToUsername.contains(socket)) {

      server.studentCount-=1
      val username = server.socketToUsername(socket)

      server.database.removeStudentFromQueue(username)

      server.socketToUsername -= socket
      if (server.usernameToSocket.contains(username)) {
        server.usernameToSocket -= username
      }
    }

    if(server.SocketToTA.contains(socket)){

      server.taCount-=1

      val usernameTA = server.SocketToTA(socket)

      server.TAonline=server.updateTaOnline(server.cred,usernameTA,false)

      server.SocketToTA -= socket

      if (server.TAtoSocket.contains(usernameTA)) {
        server.TAtoSocket -= usernameTA
      }
    }

    server.server.getBroadcastOperations.sendEvent("count", server.countJSON())
    server.broadcastQueue(server,"queue",server.queueJSON())
  }
}


class EnterQueueListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, data: String, ackRequest: AckRequest): Unit = {

    val  num:Int=server.random.nextInt(1000)

    server.loggedInClients = server.loggedInClients :+ socket

    server.studentCount+=1

    val parsed:JsValue=Json.parse(data)

    var username:String= (parsed\"username").as[String]
    username=username+"#"+num.toString()

    val topic:String= (parsed\"topic").as[String]

    val subtopic:String= (parsed\"subtopic").as[String]

    server.database.addStudentToQueue(StudentInQueue(username, System.nanoTime(),topic,subtopic))
    server.socketToUsername += (socket -> username)
    server.usernameToSocket += (username -> socket)
    server.broadcastQueue(server,"queue",server.queueJSON())

    server.server.getBroadcastOperations.sendEvent("count", server.countJSON())
  }
}


class ReadyForStudentListener(server: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socket: SocketIOClient, dirtyMessage: Nothing, ackRequest: AckRequest): Unit = {

    val queue = server.database.getQueue.sortBy(_.timestamp)
    if(queue.nonEmpty){

      val studentToHelp = queue.head
      server.database.removeStudentFromQueue(studentToHelp.username)

      socket.sendEvent("message", studentToHelp.username)

      if(server.usernameToSocket.contains(studentToHelp.username)){
        server.usernameToSocket(studentToHelp.username).sendEvent("message2", "A TA is ready to help you!")
      }

      server.broadcastQueue(server,"queue",server.queueJSON())

    }
  }
}

class displayTAListener(server:OfficeHoursServer) extends DataListener[String]{
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {

    server.database.addTA(username)

    server.loggedInClients = server.loggedInClients :+ socket

    server.SocketToTA += (socket -> username)
    server.TAtoSocket += (username -> socket)

    server.taCount+=1
    server.broadcastQueue(server,"queue",server.queueJSON())

    server.server.getBroadcastOperations.sendEvent("count", server.countJSON())

  }
}

class alertListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    val socketToSend=server.usernameToSocket(username)
    socketToSend.sendEvent("alert")
  }
}

class doneHelpingListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, username: String, ackRequest: AckRequest): Unit = {
    val TaUsername=server.SocketToTA(socket)
    server.database.addStudentHelped(TaUsername)
    val socketToSend=server.usernameToSocket(username)
    socketToSend.sendEvent("done")
  }
}

class countListener(server:OfficeHoursServer) extends DataListener[Nothing]{
  override def onData(socketIOClient: SocketIOClient, t: Nothing, ackRequest: AckRequest): Unit = {
    server.server.getBroadcastOperations.sendEvent("count", server.countJSON())
  }
}

class loginListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, message: String, ackRequest: AckRequest): Unit = {
    val loginInfo=Json.parse(message)
    val username:String= (loginInfo\"username").as[String]
    val password:String= (loginInfo\"password").as[String]

    if(server.validateLogin(server.cred,username,password,server)){
      socket.sendEvent("valid_Check")
    }
    else{
      socket.sendEvent("invalid")
    }
  }
}

class TAStatListener(server:OfficeHoursServer) extends DataListener[Nothing]{
  override def onData(socket: SocketIOClient, t: Nothing, ackRequest: AckRequest): Unit = {
    val TAinfo:Map[String,JsValue]=server.database.getTAHelpInfo()
    println(TAinfo)
    socket.sendEvent("showTable",Json.stringify(Json.toJson(TAinfo)))
  }
}

class StudentStatListener(server: OfficeHoursServer) extends DataListener[String] {
  override def onData(socket: SocketIOClient, option: String, ackRequest: AckRequest): Unit = {

    if(option=="statTopic"){
      val Studentinfo:Map[String,JsValue]=server.database.getTopicStat()
      println(Studentinfo)
      socket.sendEvent("showTopicPie",Json.stringify(Json.toJson(Studentinfo)))
    }
    else{
      val Studentinfo:Map[String,JsValue]=server.database.getSubtopicStat()
      println(Studentinfo)
      socket.sendEvent("showSubtopicPie",Json.stringify(Json.toJson(Studentinfo)))

    }

  }
}


