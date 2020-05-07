package model.database

import model.StudentInQueue
import play.api.libs.json.JsValue

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def addTA(username:String):Unit
  def addStudentHelped(usernameTA:String): Unit
  def getTAHelpInfo():Map[String,JsValue]
  def getTopicStat():Map[String,JsValue]
  def getSubtopicStat():Map[String,JsValue]



}
