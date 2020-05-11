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

  def apply(username: String, timestamp: Long, estimatedWait: String): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp, estimatedWait)
  }


}

class StudentInQueue(val username: String, val timestamp: Long, val estimatedWait: String) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp),
      "estimatedWait" -> Json.toJson(estimatedWait)
    )
    Json.toJson(messageMap)
  }

}
