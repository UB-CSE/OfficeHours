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

  def apply(qNO : Int, username: String, timestamp: Long): StudentInQueue = {
    new StudentInQueue(qNO,cleanString(username), timestamp)
  }


}

class StudentInQueue(val queueNo : Int, val username: String, val timestamp: Long) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "queueNo" -> Json.toJson(queueNo),
      "timestamp" -> Json.toJson(timestamp)
    )
    Json.toJson(messageMap)
  }
  def taAsJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "queueNo" -> Json.toJson(queueNo),
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp)
    )
    Json.toJson(messageMap)
  }


}
