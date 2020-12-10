package model.database

import model.{RegisterUser, StudentInQueue}

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def register(user : RegisterUser) : Unit
  def getUserFromDB : List[RegisterUser]
  def login(email : String, password : String) : Boolean

}
