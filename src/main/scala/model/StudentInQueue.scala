package model

import model.StudentInQueue.cleanString
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

  def apply(email: String, kindOfUser : String, timestamp: Long): StudentInQueue = {
    new StudentInQueue(cleanString(email), cleanString(kindOfUser), timestamp)
  }
}

object RegisterUser{
  def apply(fullName : String, username : String, email : String, password : String, kind : String) : RegisterUser ={
    new RegisterUser(cleanString(fullName), cleanString(username), cleanString(email), cleanString(password), cleanString(kind), 0)
  }
}

object LoginUser{
  def apply(email : String, password : String) : LoginUser ={
    new LoginUser(cleanString(email), cleanString(password))
  }
}

class StudentInQueue(val email : String, val kind : String, val timestamp: Long) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(email),
      "kindOfUser" -> Json.toJson(email),
      "timestamp" -> Json.toJson(timestamp)
    )
    Json.toJson(messageMap)
  }

}

class RegisterUser(val fullName : String, val username: String, val email : String, val password : String, val kind : String, val id : Int){
  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "fullName" -> Json.toJson(fullName),
      "username" -> Json.toJson(username),
      "email" -> Json.toJson(email),
      "password" -> Json.toJson(password),
      "kindOfUser" -> Json.toJson(kind)
    )
    Json.toJson(messageMap)
  }
}

class LoginUser(val email : String, val password : String){
  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "email" -> Json.toJson(email),
      "password" -> Json.toJson(password)
    )
    Json.toJson(messageMap)
  }
}
