package model.database

import model.StudentInQueue

trait DatabaseAPI {

  def addStudentToQueryQueue(student: StudentInQueue): Unit
  def addStudentToQuickQueue(student: StudentInQueue): Unit
  def addStudentToNormalQueue(student: StudentInQueue): Unit

  def removeStudentFromQueryQueue(username: String): Unit
  def removeStudentFromQuickQueue(username: String): Unit
  def removeStudentFromNormalQueue(username: String): Unit

  def getQueue: List[StudentInQueue]

}
