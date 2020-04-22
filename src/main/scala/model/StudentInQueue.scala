package model

import play.api.libs.json.{JsValue, Json}


object StudentInQueue {

  def convertToStudent(jsonString: String): Tuple2[String,String] = {
    val parsed: JsValue = Json.parse(jsonString)
    val username: String = (parsed \ "name").as[String]
    val issue: String = (parsed \ "issue").as[String]
    Tuple2(username,issue)
  }
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
  def cleanStringIssue(input: String): String = {
    val output = input
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
    output
  }

  def apply(username: String, timestamp: Double, issue: String =" "): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp, cleanStringIssue(issue))
  }


}

class StudentInQueue(val username: String, val timestamp: Double, val issue: String ="") {
  var positionInQueue: Int = 0
  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp),
      "issue" -> Json.toJson(issue)
    )
    Json.toJson(messageMap)
  }

}
