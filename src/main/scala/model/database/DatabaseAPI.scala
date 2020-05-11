package model.database

import model.{StudentInQueue, StudentProblem}

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def addProblemToQueue(problem: StudentProblem): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]

}
