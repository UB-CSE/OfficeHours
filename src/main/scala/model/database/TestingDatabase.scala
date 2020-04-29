package model.database

import model.{CheckedInTA, StudentInQueue}

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()

  var dataTA: List[CheckedInTA] = List()


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }


  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  override def checkInTA(ta: CheckedInTA): Unit = {
    dataTA ::= ta
  }

  override def checkOutTA(username: String): Unit = {
    dataTA = dataTA.filter(_.username != username)
  }

  override def getTA: List[CheckedInTA] = {
    dataTA
  }

}
