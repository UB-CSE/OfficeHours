package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.{CheckedInTA, StudentInQueue}


class Database extends DatabaseAPI{
  //"jdbc:mysql://mysql/officehours?autoReconnect=true"
  val url = "jdbc:mysql://localhost/mysql?serverTimezone=UTC"
  val username: String = "root"
  val password: String = "Andy123"

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()

  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT)")
    statement.execute("CREATE TABLE IF NOT EXISTS assistants (username TEXT, timestamp BIGINT)")
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

  override def checkInTA(ta: CheckedInTA): Unit = {
    val statement = connection.prepareStatement("INSERT INTO assistants VALUE(?,?)")

    statement.setString(1, ta.username)
    statement.setLong(2, ta.timestamp)

    statement.execute()
  }

  override def checkOutTA(username: String): Unit = {
    val statement = connection.prepareStatement("DELETE FROM assistants WHERE username=?")

    statement.setString(1, username)

    statement.execute()
  }

  override def getTA: List[CheckedInTA] = {
    val statement = connection.prepareStatement("SELECT * FROM assistants")
    val result: ResultSet = statement.executeQuery()

    var ta_list: List[CheckedInTA] = List()

    while(result.next()){
      val username = result.getString("username")
      val timestamp = result.getLong("timestamp")
      ta_list ::= new CheckedInTA(username, timestamp)
    }
    ta_list
  }

}
