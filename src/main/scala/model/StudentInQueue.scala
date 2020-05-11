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

  def apply(username: String, timestamp: Long, ticket: Int): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp, ticket)
  }


}

class StudentInQueue(val username: String, val timestamp: Long, ticket: Int) {
  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "ticket" -> Json.toJson(ticket),
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp)
    )
    Json.toJson(messageMap)
  }

}
