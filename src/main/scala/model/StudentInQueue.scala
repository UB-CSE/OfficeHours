package model

import play.api.libs.json.{JsValue, Json}
import java.util.{Calendar, TimeZone}


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

  def apply(username: String, timestamp: Long, queuePosition: Int): StudentInQueue = {
    new StudentInQueue(cleanString(username), timestamp, getTime(), queuePosition)
  }

  def getTime(): String = {
    val time: Calendar = Calendar.getInstance()
    var currentMinute: String = time.get(Calendar.MINUTE).toString
    var currentHour: String = time.get(Calendar.HOUR).toString
    val currentA: Int = time.get(Calendar.AM_PM)
    //fixes minutes format, without this anything under 10 will be displayed as 6:5 instead of 6:05
    if(currentMinute.toInt < 10){
      currentMinute = "0" + currentMinute
    }
    if(currentHour.toInt == 0){
      currentHour = "12"
    }

    if(currentA == 1){
      currentHour + ":" + currentMinute + " PM"
    }else{
      currentHour + ":" + currentMinute + " AM"
    }
  }



}
//added time in Hour:Min AM/PM format, replaces nanoseconds on screen
class StudentInQueue(val username: String, val timestamp: Long, val worldTime: String, var queuePosition: Int) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "timestamp" -> Json.toJson(timestamp),
      "worldTime" -> Json.toJson(worldTime),
      "queuePosition" -> Json.toJson(queuePosition)
    )
    Json.toJson(messageMap)
  }

}
