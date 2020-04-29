package model.database

import model.StudentInQueue

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def addUserToAuthenticate(username: String, hashpass: String, salt: String): Boolean
  def authenticate(username: String, hashpass: String): String
}
