package model.database

import model.StudentInQueue

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }

  override def updateStudentPosition(): Unit = {
    data.foreach(_.queuePosition -= 1)
  }

  override def getStudent(username: String): StudentInQueue = {
    for(student <- data if student.username == username){
      return student
    }
    println("Could not find " + username + "'s data")
    //not a fan of having it return this, but it should never get to this point anyways
    new StudentInQueue("could not find user data", 0, "check getStudent", -5)
  }

  override def studentLeftUpdatePosition(leftPosition: Int): Unit = {
    for (student <- data if student.queuePosition > leftPosition) {
      student.queuePosition -= 1
    }
  }

  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

}
