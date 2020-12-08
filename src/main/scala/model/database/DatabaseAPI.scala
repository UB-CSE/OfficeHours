package model.database

import model.{Staff, StudentInQueue}

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def getStaff: List[Staff]
  def addStaff(staff: Staff): Unit

}
