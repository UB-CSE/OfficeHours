package model

import play.api.libs.json.{JsValue, Json}

class Staff(val ubit: String, val password: String) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "ubit" -> Json.toJson(ubit),
      "password" -> Json.toJson(password)
    )
    Json.toJson(messageMap)
  }

}
