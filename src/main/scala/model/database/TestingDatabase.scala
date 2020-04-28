package model.database

import model.StudentInQueue

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()

//Genevieve Gray's Edit: No duplicate names, no empty names
  override def addStudentToQueue(student: StudentInQueue): Unit = {
    if(student.username != ""){
      var alreadyExists = false
      for(i <- data){
        if(student.username == i.username){
          alreadyExists = true
        }
      }
      if(!alreadyExists){
        data ::= student
      }
    }
  }


  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

}
