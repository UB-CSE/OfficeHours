package model.database

import model.{LookupResult, StudentInQueue}

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def studentLookup(username: String): LookupResult
}
