package model

import play.api.libs.json.{JsValue, Json}


object StudentProblem {

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

  def apply(problem: String): StudentProblem = {
    new StudentProblem(cleanString(problem))
  }

}

class StudentProblem(val problem: String) {

  def asJsValue(): JsValue = {
    Json.toJson(problem)
  }

}
