package model.database

import model.StudentInQueue

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  //my functions added to API
  def addTAToAvailable(taName: String): Unit
  def removeTAFromAvailable(taName: String): Unit

}
