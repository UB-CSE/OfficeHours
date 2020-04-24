package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue

class Database extends DatabaseAPI {

  val url = "jdbc:mysql://mysql/officehours"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT, issue TEXT)")
  }



  def addStudentToQueue(student: StudentInQueue): Unit = {
    val studentApplied: StudentInQueue = StudentInQueue.apply(student.username,student.timestamp,student.issue)

    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?, ?)")

    statement.setString(1, studentApplied.username)
    statement.setDouble(2, studentApplied.timestamp)
    statement.setString(3,studentApplied.issue)

    statement.execute()
  }


  def removeStudentFromQueue(username: String): Unit = {
    val statement = connection.prepareStatement("DELETE FROM queue WHERE username=?")

    statement.setString(1, username)

    statement.execute()
  }


  def getQueue(): List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queue")
    val result: ResultSet = statement.executeQuery()

    var queue: List[StudentInQueue] = List()

    while (result.next()) {
      val username = result.getString("username")
      val timestamp = result.getDouble("timestamp")
      val issue = result.getString("issue")
      queue = new StudentInQueue(username, timestamp, issue) :: queue
    }
    queue = queue.sortBy(_.timestamp)
    for(student <- queue){
      student.positionInQueue = queue.indexOf(student) + 1
    }
    queue
  }

}
