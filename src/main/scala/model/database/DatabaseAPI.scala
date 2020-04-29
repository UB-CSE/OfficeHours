package model.database

import model.StudentInQueue
import model.CheckedInTA


trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def checkInTA(ta: CheckedInTA): Unit
  def checkOutTA(username: String): Unit
  def getTA: List[CheckedInTA]

}
