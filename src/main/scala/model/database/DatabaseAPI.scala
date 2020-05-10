package model.database

import model.StudentInQueue

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]

  def addFeedback(username: String, feedback: String): Unit
  def getAllFeedback: Map[String,String]

}
