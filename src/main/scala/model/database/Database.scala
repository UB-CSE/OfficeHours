package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}


class Database extends DatabaseAPI{

  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT)")
    statement.execute("CREATE TABLE IF NOT EXISTS totalVisits(username TEXT, amount TEXT)")
    statement.execute("CREATE TABLE IF NOT EXISTS reason(username TEXT, dateS TEXT, reason TEXT)")
  }

  def isIThere(username:String): Int = {
    val statement = connection.prepareStatement("SELECT EXISTS(SELECT * FROM reason WHERE username = (?))")
    statement.setString(1,username)
    val result: ResultSet = statement.executeQuery()
    var tr: Int = 0
    while (result.next()){
      tr = result.getInt(1)
    }
    println(tr + " is the value of EXIST")
    tr
  }

  def reasonHolder(student: String, date:String, reason:String): Unit = {
    val statement = connection.prepareStatement("INSERT INTO reasons VALUE(?,?,?)")
    statement.setString(1,student)
    statement.setString(2,reason)
    statement.setString(3,date)
  }
  def getReasons(username:String): List[String] = {
    val statement = connection.prepareStatement("SELECT * FROM reasons WHERE username=(?)")
    statement.setString(1,username)
    val result: ResultSet = statement.executeQuery()
    var listR: List[String] = List()
    while(result.next()){
      listR = listR :+ s"(${result.getString("reason") + " " + result.getString("dateS")})"
    }
    listR.reverse
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
