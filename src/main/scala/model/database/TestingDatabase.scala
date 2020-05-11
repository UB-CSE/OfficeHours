package model.database

import model.{StudentInQueue, StudentProblem}

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()
  var data1: List[StudentProblem] = List()


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }


  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }

  override def addProblemToQueue(problem: StudentProblem): Unit = {
    data1 :: = problem
  }



}
