package model

import play.api.libs.json.{JsValue, Json}


object StudentInQueue {

  def cleanString(input: String): String = {
    var output = input
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
    if (output.length > 30) {
      output = output.slice(0, 30) + "..."
    }
    output
  }

  def apply(username: String, timestamp: Long, topic:String, subtopic:String): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp,topic,subtopic)
  }


}

class StudentInQueue(val username: String, val timestamp: Long, val topic:String, val subtopic:String) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp),
      "topic"->Json.toJson(topic),
      "subtopic"->Json.toJson(subtopic)
    )
    Json.toJson(messageMap)
  }

}
