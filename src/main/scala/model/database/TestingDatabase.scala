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

  // get position of a given student
  override def getPosition(username: String): Int = {
    for (student <- data) {
      if (student.username == username) {
        return student.position
      }
    }
   -1
  }

  // update the position of a given student
  override def updatePosition(username: String, newPosition: Int): Unit = {
    for (entry <- data) {
      if (entry.username == username) {
        entry.position = newPosition
      }
    }
  }

  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }
}
