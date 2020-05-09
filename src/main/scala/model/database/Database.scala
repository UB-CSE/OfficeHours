package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue


class Database extends DatabaseAPI{

  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, description TEXT, timestamp BIGINT)")
    statement.execute("CREATE TABLE IF NOT EXISTS traffic (username TEXT, description TEXT, timestamp BIGINT)")
  }


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?, ?)")

    statement.setString(1, student.username)
    statement.setString(2, student.description)
    statement.setLong(3, student.timestamp)

    statement.execute()

    val otherStatement = connection.prepareStatement("INSERT INTO traffic VALUE (?, ?, ?)")

    otherStatement.setString(1, student.username)
    otherStatement.setString(2, student.description)
    otherStatement.setLong(3, student.timestamp)

    statement.execute()
  }


  override def removeStudentFromQueue(username: String): Unit = {
    val statement = connection.prepareStatement("DELETE FROM queue WHERE username=?")

    statement.setString(1, username)

    statement.execute()
  }

  override def getStudentUser(user: String): String = {
    val statement = connection.prepareStatement("SELECT username FROM queue WHERE username=?")

    statement.setString(1, user)

    val result: ResultSet = statement.executeQuery()

    if (result.next()) {
      val returnedUser = result.getString("username")
      returnedUser
    }else ""

  }


  override def getQueue: List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queue")
    val result: ResultSet = statement.executeQuery()

    var queue: List[StudentInQueue] = List()

    while (result.next()) {
      val username = result.getString("username")
      val description = result.getString("description")
      val timestamp = result.getLong("timestamp")
      queue = new StudentInQueue(username, description, timestamp) :: queue
    }

    queue.reverse
  }

  override def getTraffic: List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM traffic")
    val result: ResultSet = statement.executeQuery()

    var trafficHistory: List[StudentInQueue] = List()

    while (result.next()) {
      val username = result.getString("username")
      val description = result.getString("description")
      val timestamp = result.getLong("timestamp")
      trafficHistory = new StudentInQueue(username, description, timestamp) :: trafficHistory
    }

    trafficHistory.reverse

  }

}
