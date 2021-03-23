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

  override def checkTACredentials(credentials: String): Boolean = {
    true
  }

  override def getStudent_queue(user: String): List[StudentInQueue] = {
    data.filter(_.username != user)
  }

}
