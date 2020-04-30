package model.database

import model.{Configuration, LookupResult, StudentInQueue}

case class Record(username: String, timestamp: Long, var archived: Boolean)

class TestingDatabase extends DatabaseAPI {

  var data: List[Record] = List()

  override def addStudentToQueue(student: StudentInQueue): Unit = data ::= Record(student.username, student.timestamp, archived = false)

  override def removeStudentFromQueue(username: String): Unit = {
    if(Configuration.ARCHIVE_MODE) data.filter(_.username == username).head.archived = true
    else data = data.filter(_.username != username)
  }

  override def getQueue: List[StudentInQueue] = {
    data.filter(_.archived == false).map(r => StudentInQueue(r.username, r.timestamp))
  }

  override def studentLookup(username: String): LookupResult = LookupResult(username, data.filter(_.username == username).map(_.timestamp))
}
