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
    statement.execute("CREATE TABLE IF NOT EXISTS queryQueue (username TEXT, timestamp BIGINT)")
    statement.execute("CREATE TABLE IF NOT EXISTS quickQueue (username TEXT, timestamp BIGINT)")
    statement.execute("CREATE TABLE IF NOT EXISTS normalQueue (username TEXT, timestamp BIGINT)")
  }


  override def addStudentToQueryQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queryQueue VALUE (?, ?, ?)")

    statement.setString(1, student.username)
    statement.setLong(2, student.timestamp)
    statement.setString(3, student.message)

    statement.execute()
  }

  override def removeStudentFromQueryQueue(username: String): Unit = {
    val statement = connection.prepareStatement("DELETE FROM queryQueue WHERE username=?")

    statement.setString(1, username)

    statement.execute()
  }

  override def addStudentToQuickQueue(student: StudentInQueue): Unit = { //never used
    val statement = connection.prepareStatement("INSERT INTO quickQueue VALUE (?, ?)")

    statement.setString(1, student.username)
    statement.setLong(2, student.timestamp)

    statement.execute()
  }

  override def addStudentToNormalQueue(student: StudentInQueue): Unit = { //never used
    val statement = connection.prepareStatement("INSERT INTO normalQueue VALUE (?, ?)")

    statement.setString(1, student.username)
    statement.setLong(2, student.timestamp)

    statement.execute()
  }

  override def removeStudentFromQuickQueue(student: String): Unit = { //never used
    val statement = connection.prepareStatement("DELETE FROM quickQueue where username=?")

    statement.setString(1, student)

    statement.execute()
  }


  override def removeStudentFromNormalQueue(username: String): Unit = {
    val statement = connection.prepareStatement("DELETE FROM normalQueue WHERE username=?")

    statement.setString(1, username)

    statement.execute()
  }


  override def getQueue: List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queryQueue")
    val result: ResultSet = statement.executeQuery()

    var queryQueue: List[StudentInQueue] = List()

    while (result.next()) {
      val username = result.getString("username")
      val timestamp = result.getLong("timestamp")
      var message = result.getString("message")
      queryQueue = new StudentInQueue(username, timestamp, message) :: queryQueue
    }

    queryQueue.reverse
  }

}
