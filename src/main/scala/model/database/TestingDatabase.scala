package model.database

import model.StudentInQueue

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()
  var taData: List[String] = List()


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }


  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  // Database testing for adding TAs
  override def addTAToAvailable(taName: String): Unit = {
    taData ::= taName
  }

  // Database testing for removing TAs
  override def removeTAFromAvailable(taName: String): Unit = {
    taData = taData.filter(_ != taName)
  }

}
