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

  def apply(username: String, timestamp: Long, description: String): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp, cleanString(description))
  }


}

class StudentInQueue(val username: String, val timestamp: Long, val description: String) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp),
      "description" -> Json.toJson(description)
    )
    Json.toJson(messageMap)
  }

}
