package model.database

import model.StudentInQueue

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def updateStudentPosition(): Unit
  def studentLeftUpdatePosition(leftPosition: Int): Unit
  def getStudent(username: String): StudentInQueue
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def clearQueue(): Unit = {}

}
