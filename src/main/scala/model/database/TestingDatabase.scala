package model.database

import model.StudentInQueue

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }


  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
    println(data)

  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  override def queuePosition(username: String): Int = {
    var position: Int = 0
    println(data)
    val reversed = data.reverse
    for (student <- reversed) {
      if (student.username == username) {
        student.position = reversed.indexOf(student) + 1
        position = student.position
      }
    }
    position
  }

  override def queuePositionUpdate: Int = {
    //println(data)
    var position: Int = 0
    for (student <- data) {
      student.position = student.position - 1
      position = student.position
    }
    println(position)
    position
  }
}
