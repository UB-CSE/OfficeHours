package model.database

import model.{Staff, StudentInQueue}

class TestingDatabase extends DatabaseAPI {

  val serverAdmin: Staff = new Staff("serveradmin","adminpassword2")
  var data: List[StudentInQueue] = List()
  var staff: List[Staff] = List(serverAdmin)

  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }


  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  override def getStaff: List[Staff] = {
    staff
  }

  override def addStaff(newStaff: Staff): Unit = {
    this.staff = this.staff ::: List(newStaff)
  }
}
