package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue
import play.api.libs.json.Json


//noinspection DuplicatedCode
class Database extends DatabaseAPI{

//  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
//  val username: String = sys.env("DB_USERNAME")
//  val password: String = sys.env("DB_PASSWORD")
//
  val url = "jdbc:mysql://localhost/officehours?&serverTimezone=UTC"
  val username: String = "root"
  val password: String = "password"

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT)")
    val statement2 = connection.createStatement()
    statement2.execute("CREATE TABLE IF NOT EXISTS authentication (username TEXT, password TEXT)")
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

  override def sendQueueStudent(user: String): List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queue")
    val result: ResultSet = statement.executeQuery()

    var queue: List[StudentInQueue] = List()
    var counter: Int = 1
    while (result.next()) {

      var username = result.getString("username")
      if (username == user) username = user
      else username = "Student " + counter.toString
      val timestamp = result.getLong("timestamp")
      queue = new StudentInQueue(username, timestamp) :: queue
      counter += 1
    }

    queue
  }

  override def checkTACredentials(credentials: String): Boolean = {
    val paresd = Json.parse(credentials)
    val given_user: String = (paresd \ "username").as[String]
    val given_password: String = (paresd \ "password").as[String]
    val statement = connection.prepareStatement("SELECT * FROM authentication")
    val result: ResultSet = statement.executeQuery()
    while (result.next()) {
      val username = result.getString("username")
      val password = result.getString("password")
      if (username == given_user && password == given_password) {
        println("LOGIN SUCCESSFUL")
        return true
      }
    }
    false
  }
}
