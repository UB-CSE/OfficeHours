package model.database

import java.sql.{Connection, DriverManager, ResultSet}
import java.time.LocalDateTime

import model.StudentInQueue


class Database extends DatabaseAPI{

  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT)")
  }

  def setupStatsTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS stats (username TEXT, dateTime String)")
  }

  def numberTimes(username: String): Int = {
    var count: Int = 0
    val statement = connection.prepareStatement("SELECT * FROM stats WHERE username = ?")

    statement.setString(1, username)

    val result: ResultSet = statement.executeQuery()

    while(result.next()){
      count += 1
    }

    count


  }


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?)")

    statement.setString(1, student.username)
    statement.setLong(2, student.timestamp)

    statement.execute()

    val statement2 = connection.prepareStatement("INSERT INTO stats VALUE (?, ?)")

    statement2.setString(1, student.username)
    statement2.setString(2, java.time.LocalDateTime.now().toString())

    statement2.execute()
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
