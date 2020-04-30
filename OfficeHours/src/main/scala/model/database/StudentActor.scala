package model.database

import akka.actor.Actor
import play.api.libs.json.{JsValue, Json}
import model._


case object MoveDown
case object GetPosition
case class SetPosition(pos: Int)

class StudentActor(username: String) extends Actor{
  var position = 0
  override def receive: Receive = {
    case MoveDown =>
      println(username + " was moved down1")
      position -= 1
    case GetPosition =>
      var jsonMap: Map[String, JsValue] = Map(
        "username" -> Json.toJson(username),
        "position" -> Json.toJson(position)
      )
      sender() ! StudentState(Json.stringify(Json.toJson(jsonMap)))
    case recieved: SetPosition =>
      position = recieved.pos
      println(username + " is waiting in pos " + position)
      this.self ! GetPosition
  }
}
