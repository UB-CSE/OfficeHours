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

  def cleanString2(input: String): String = {
    var output = input
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
    if (output.length > 200) {
      output = output.slice(0, 200) + "..."
    }
    output
  }



  def apply(username: String, timestamp: Long, reason: String): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp, cleanString2(reason))
  }

}

class StudentInQueue(val username: String, val timestamp: Long, val reason: String) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp),
      "reason" -> Json.toJson(reason)
    )
    Json.toJson(messageMap)
  }

}
