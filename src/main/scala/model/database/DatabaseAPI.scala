package model.database

import java.security.KeyStore.PasswordProtection

import model.StudentInQueue

trait DatabaseAPI {

  def addStudentToQueue(student: StudentInQueue): Unit
  def removeStudentFromQueue(username: String): Unit
  def getQueue: List[StudentInQueue]
  def reasonHolder(student: String,date:String, reason:String)
  def getReasons(username:String): List[String]
  def isIThere(username:String): Int
  def storeLogin(username:String,password:String): Unit
  def checkLogin(username:String,password:String): Boolean

}
