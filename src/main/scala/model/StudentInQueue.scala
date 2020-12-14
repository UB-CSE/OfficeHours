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

  def apply(username: String, timestamp: Long): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp)
  }


}

class StudentInQueue(val username: String, val timestamp: Long) {

  val maxTime:Double=(Math.random()*11)+10 //max time a student in queue gets to spend in office hours with a TA, 10-20 minutes

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp)
    )
    Json.toJson(messageMap)
  }

}
