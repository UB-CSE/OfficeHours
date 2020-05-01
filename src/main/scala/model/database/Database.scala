package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue
import play.api.libs.json.Json
import org.mindrot.jbcrypt.BCrypt


//noinspection DuplicatedCode
class Database extends DatabaseAPI {

  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT)")
    val statement2 = connection.createStatement()
    statement2.execute("CREATE TABLE IF NOT EXISTS authentication (username TEXT, password TEXT)")

    // Purely for Testing purposes; ideally TA's would have a registering system
    val statement3 = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM authentication WHERE username = '116_ta') ")
    if (statement3.execute()) {
      val statement4 = connection.prepareStatement("INSERT INTO authentication VALUE (?, ?)")
      statement4.setString(1, "116_ta")
      statement4.setString(2, BCrypt.hashpw("changeme", BCrypt.gensalt(5)))
      statement4.execute()
    }
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
    queue
  }

  override def getStudent_queue(user: String): List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queue")
    val result: ResultSet = statement.executeQuery()

    var queue: List[StudentInQueue] = List()
    var counter: Int = 1
    while (result.next()) {

      var username = result.getString("username")
      if (username.length > 20) username = username.slice(0, 20) + "..."
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
    val statement = connection.prepareStatement("SELECT * FROM authentication WHERE username = ?")
    statement.setString(1, given_user)
    val result: ResultSet = statement.executeQuery()

    while (result.next()) {
      val username = result.getString("username")
      val password = result.getString("password")
      if (username == given_user && BCrypt.checkpw(given_password, password)) {
        println("LOGIN SUCCESSFUL")
        return true
      }
    }
    false
  }
}
