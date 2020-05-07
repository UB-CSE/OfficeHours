package model

import play.api.libs.json.{JsValue, Json}
import model.database.TimeStrings
object StudentInQueue {

  def cleanString(input: String): String = {
    var output = input
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
    if (output.length > 20) {
      output = output.slice(0, 20) + "..."
    }
    output
  }

  def apply(username: String, timestamp: Long): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp)
  }


}

class StudentInQueue(val username: String, val timestamp: Long) {



  def asJsValue(): JsValue ={
    var time = TimeStrings.getDateTime()
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(time),
    )
    Json.toJson(messageMap)
  }

}
