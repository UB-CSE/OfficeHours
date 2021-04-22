package model.database

import model.StudentInQueue

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }

  override def moveToEndOfQueue(student: StudentInQueue): Unit = {
    data = data.filter(_.username != student.username)
    var latestTime = data.head.timestamp
    val newStudent = new StudentInQueue(student.username,student.helpDescription,latestTime+1)
    data ::= newStudent
  }

  override def clearQueue: Unit = {
    data = List()
  }

  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }

  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  override def checkTACredentials(credentials: String): Boolean = {
    true
  }

  override def getStudent_queue(user: String): List[StudentInQueue] = {
    data.filter(_.username != user)
  }

}
