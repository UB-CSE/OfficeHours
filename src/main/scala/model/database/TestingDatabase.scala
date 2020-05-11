package model.database

import model.StudentInQueue

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()


  override def addStudentToQueryQueue(student: StudentInQueue): Unit = {
    data ::= student
  }

  override def addStudentToQuickQueue(student: StudentInQueue): Unit = {
    data ::= student
  }

  override def addStudentToNormalQueue(student: StudentInQueue): Unit = {
    data ::= student
  }

  override def removeStudentFromQueryQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }

  override def removeStudentFromQuickQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }

  override def removeStudentFromNormalQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

}
