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
    new StudentInQueue("", cleanString(username), "", "", "",  timestamp)
  }


}

class StudentInQueue(val fullName : String, val username: String, val email : String, val password : String, val kind : String, val timestamp: Long) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp)
    )
    Json.toJson(messageMap)
  }

  //Get a username from the json file
  def getUsername(jsonString : String) : String = {
    ""
  }

}
