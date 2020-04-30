package model

import com.corundumstudio.socketio.{AckRequest, SocketIOClient}
import com.corundumstudio.socketio.listener.DataListener
import play.api.libs.json.{JsValue, Json}

import scala.util.Random

class StudentLogin {

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
      server.liTa.foreach(_.sendEvent("queue", server.taQueueJSON()))
    }
  }
}

class GotATa ( officeHoursServer: OfficeHoursServer) extends DataListener[Nothing]{
  override def onData(socketIOClient: SocketIOClient, t: Nothing, ackRequest: AckRequest): Unit = {
    println("got a ta")
    officeHoursServer.liTa = officeHoursServer.liTa :+ socketIOClient
    socketIOClient.sendEvent("queue", officeHoursServer.taQueueJSON())
  }
}

class QueueStudent(officeHoursServer: OfficeHoursServer) extends DataListener[String]{

  override def onData(socketIOClient: SocketIOClient, JsString: String, ackRequest: AckRequest): Unit = {
    println(JsString)
    val parsed : JsValue = Json.parse(JsString)
    val ubit : String = (parsed \ "ubit").as[String]
    val pass : String = (parsed\ "pass").as[String]
    if (pass.toInt == officeHoursServer.passcode){
      officeHoursServer.database.addStudentToQueue(StudentInQueue(officeHoursServer.queueNo, ubit, System.nanoTime()))
      officeHoursServer.socketToUsername += (socketIOClient -> ubit)
      officeHoursServer.usernameToSocket += (ubit -> socketIOClient)

      socketIOClient.sendEvent("message", "You are  number " + officeHoursServer.queueNo.toString )

      officeHoursServer.passcode = officeHoursServer.randomSixInt()
      officeHoursServer.queueNo += 1

      officeHoursServer.passSocket.sendEvent("passCode",Json.stringify(Json.toJson(officeHoursServer.passcode)))
      officeHoursServer.server.getBroadcastOperations.sendEvent("queue", officeHoursServer.queueJSON())
      officeHoursServer.liTa.foreach(_.sendEvent("queue", officeHoursServer.taQueueJSON()))
    }
  }
}

class Passcode (officeHoursServer: OfficeHoursServer) extends DataListener[Nothing] {
  override def onData(socketIOClient: SocketIOClient, t: Nothing, ackRequest: AckRequest): Unit = {
    officeHoursServer.passSocket = socketIOClient
    socketIOClient.sendEvent("passCode",Json.stringify(Json.toJson(officeHoursServer.passcode)))
  }
}
