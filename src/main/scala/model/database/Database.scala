package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.{Configuration, LookupResult, StudentInQueue}


class Database extends DatabaseAPI{

  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT, archived BOOLEAN)")
  }


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?, 0)")

    statement.setString(1, student.username)
    statement.setLong(2, student.timestamp)

    statement.execute()
  }


  override def removeStudentFromQueue(username: String): Unit = {
    val queryString: String = if (Configuration.ARCHIVE_MODE) {
      "UPDATE queue SET archived=1 WHERE username=?;"
    }else{
      "DELETE FROM queue WHERE username=?"
    }
    val statement = connection.prepareStatement(queryString)

    statement.setString(1, username)

    statement.execute()
  }


  override def getQueue: List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queue WHERE archived=0;")
    val result: ResultSet = statement.executeQuery()

    var queue: List[StudentInQueue] = List()

    while (result.next()) {
      val username = result.getString("username")
      val timestamp = result.getLong("timestamp")
      queue = new StudentInQueue(username, timestamp) :: queue
    }

    queue.reverse
  }

  override def studentLookup(username: String): LookupResult = {
    val statement = connection.prepareStatement("SELECT * FROM queue WHERE username=?;")
    statement.setString(1, username)
    val result: ResultSet = statement.executeQuery()

    var history: List[Long] = List()
    while (result.next()) {
      val timestamp = result.getLong("timestamp")
      history = timestamp :: history
    }
    LookupResult(username, history)
  }
}
