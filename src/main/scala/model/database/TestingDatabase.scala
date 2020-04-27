package model.database

import model.StudentInQueue

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()
  var feedback: List[String] = List()


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }

  override def addFeedback(message: String): Unit = {
    feedback ::= message
  }


  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  override def getfbQueue: List[String] = {
    feedback.reverse
  }

}
