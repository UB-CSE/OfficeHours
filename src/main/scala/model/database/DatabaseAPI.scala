package model.database

import model.StudentInQueue

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def checkTACredentials(credentials: String): Boolean
  def getStudent_queue(user: String): List[StudentInQueue]
}
