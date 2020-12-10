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
    statement.execute("CREATE TABLE IF NOT EXISTS queue (fullname TEXT, username TEXT, email TEXT, password TEXT, kind TEXT, timestamp BIGINT)")
  }


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?, ?, ?, ?)")

    statement.setString(1, student.fullName)
    statement.setString(1, student.username)
    statement.setString(1, student.email)
    statement.setString(1, student.password)
    statement.setString(1, student.kind)
    statement.setLong(2, student.timestamp)

    statement.execute()
  }


  override def removeStudentFromQueue(username: String): Unit = {
    val statement = connection.prepareStatement("DELETE FROM queue WHERE username=?")

    statement.setString(1, username)

    statement.execute()
  }


  override def getQueue: List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queue") //Get everything from DB
    val result: ResultSet = statement.executeQuery()

    var queue: List[StudentInQueue] = List()

    while (result.next()) {
      val fullName = result.getString("fullName")
      val username = result.getString("username")
      val email = result.getString("email")
      val password = result.getString("password")
      val kind = result.getString("kind")
      val timestamp = result.getLong("timestamp")
      queue = new StudentInQueue(fullName, username, email, password, kind, timestamp) :: queue
    }

    queue.reverse
  }

}
