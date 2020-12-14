package model

import java.text.SimpleDateFormat
import java.util.Calendar

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

  val c: Calendar = Calendar.getInstance
  val hr12 = new SimpleDateFormat("hh")
  val formhr12: String = hr12.format(c.getTime)
  val min = new SimpleDateFormat("mm")
  val formmin: String = min.format(c.getTime)
  val sec = new SimpleDateFormat("ss")
  val formsec: String = sec.format(c.getTime)
  val a_p = new SimpleDateFormat("a")
  val forma_p: String = a_p.format(c.getTime)

  val time: String = (formhr12 + ":" + formmin + ":" + formsec + " " + forma_p)

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(time)
    )
    Json.toJson(messageMap)
  }

}
