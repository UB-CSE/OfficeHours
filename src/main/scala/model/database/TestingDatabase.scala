package model.database

import model.{RegisterUser, StudentInQueue}

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()
  var userData : List[RegisterUser] = List()

  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }


  override def removeStudentFromQueue(email: String): Unit = {
    data = data.filter(_.email != email)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  override def register(user: RegisterUser): Unit = {
    userData ::= user
  }

  override def getUserFromDB: List[RegisterUser] = {
    userData.reverse
  }

  override def login(email: String, password: String): Boolean = {
    true
  }
}
