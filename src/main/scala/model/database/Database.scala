package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue


class Database extends DatabaseAPI{

  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")


  var connection: Connection = DriverManager.getConnection(url, username, password)


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT, position INT)")
  }


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?, ?)")

    statement.setString(1, student.username)
    statement.setLong(2, student.timestamp)
    statement.setInt(3, student.position)

    statement.execute()
  }


  override def removeStudentFromQueue(username: String): Unit = {
    val statement = connection.prepareStatement("DELETE FROM queue WHERE username=?")

    statement.setString(1, username)

    statement.execute()
  }


  // get position of a given student
  override def getPosition(student: String): Int = {
    val statement = connection.prepareStatement("SELECT * FROM queue WHERE username=?")
    statement.setString(1, student)
    val result: ResultSet = statement.executeQuery()

    var position: Int = 0

    while(result.next()) {
      position = result.getInt("position")
    }
    position
  }

  // update the position of a given student
  override def updatePosition(username: String, newPosition: Int): Unit = {
    val statement = connection.prepareStatement("UPDATE queue SET position=? WHERE username=?")

    statement.setInt(1, newPosition)
    statement.setString(2, username)

    statement.execute()
  }


  override def getQueue: List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queue")
    val result: ResultSet = statement.executeQuery()

    var queue: List[StudentInQueue] = List()

    while (result.next()) {
      val username = result.getString("username")
      val timestamp = result.getLong("timestamp")
      val position = result.getInt("position")
      queue = new StudentInQueue(username, timestamp, position) :: queue
    }

    queue.reverse
  }
}
