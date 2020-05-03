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


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  override def addTA(username:String):Unit={

  }
  override def addStudentHelped(usernameTA:String): Unit={

  }

  override def getTAHelpInfo():Map[String,Int]={
    ???
  }
  override def getTopicStat():Map[String,Int]={
    ???
  }
  override def getSubtopicStat():Map[String,Int]={
    ???
  }

}
