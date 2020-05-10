package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue


class Database extends DatabaseAPI{

  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()
  setupFeedbackTable()

  def setupFeedbackTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS feedback (username TEXT, feedback TEXT)")
  }

  override def addFeedback(username: String, feedback: String): Unit = {
    val statement = connection.prepareStatement("INSERT INTO feedback VALUE (?, ?)")

    statement.setString(1, username)
    statement.setString(2, feedback)

    statement.execute()
  }

  override def getAllFeedback(): Map[String,String] = {
    val statement = connection.prepareStatement("SELECT * FROM feedback")
    val result: ResultSet = statement.executeQuery()

    var allfeedback: Map[String, String] = Map()

    while (result.next()) {
      val username = result.getString("username")
      val feedback = result.getString("feedback")
      allfeedback += (username -> feedback)
    }
    allfeedback
  }

  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT)")
  }

  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?)")

    statement.setString(1, student.username)
    statement.setLong(2, student.timestamp)

    statement.execute()
  }


  override def removeStudentFromQueue(username: String): Unit = {
    val statement = connection.prepareStatement("DELETE FROM queue WHERE username=?")

    statement.setString(1, username)

    statement.execute()
  }


  override def getQueue: List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queue")
    val result: ResultSet = statement.executeQuery()

    var queue: List[StudentInQueue] = List()

    while (result.next()) {
      val username = result.getString("username")
      val timestamp = result.getLong("timestamp")
      queue = new StudentInQueue(username, timestamp) :: queue
    }

    queue.reverse
  }

}
