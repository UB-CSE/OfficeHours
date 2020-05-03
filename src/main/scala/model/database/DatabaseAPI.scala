package model.database

import model.StudentInQueue

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def addTA(username:String):Unit
  def addStudentHelped(usernameTA:String): Unit
  def getTAHelpInfo():Map[String,Int]
  def getTopicStat():Map[String,Int]
  def getSubtopicStat():Map[String,Int]



}
