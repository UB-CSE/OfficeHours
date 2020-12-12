package model

import play.api.libs.json.{JsValue, Json}


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

  def apply(username: String, timestamp: Long, position: Int): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp, position)
  }


}

class StudentInQueue(val username: String, val timestamp: Long, val position: Int) {

  def asJsValue(): JsValue = {
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp),
      "position"-> Json.toJson(position)
    )
    Json.toJson(messageMap)
  }

}
