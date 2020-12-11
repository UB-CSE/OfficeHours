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

  def apply(username: String,description: String, timestamp: Long): StudentInQueue = {
    new StudentInQueue(cleanString(username),cleanString(description), timestamp)
  }


}

class StudentInQueue(val username: String, val description: String, val timestamp: Long) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "description" -> Json.toJson(description),
      "timestamp" -> Json.toJson(timestamp)
    )
    Json.toJson(messageMap)
  }

}
