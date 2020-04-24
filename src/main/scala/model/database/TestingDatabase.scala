
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


  override def getQueue(): List[StudentInQueue] = {
    data = data.sortBy(_.timestamp)
    for (studentindex <- data.indices) {
      data(studentindex).positionInQueue = studentindex+1
    }
    data

  }
}
