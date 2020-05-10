package model.database

import model.StudentInQueue

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }


  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  override def reasonHolder(student: String,dateS: String, reason: String): Unit = {

  }

  override def getReasons(username: String): List[String] = {
    List()
  }

  override def isIThere(username:String): Int = {
    0
  }
}
