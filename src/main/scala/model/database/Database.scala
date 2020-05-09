package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue


class Database extends DatabaseAPI{

  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")
//  val url = "jdbc:mysql://localhost/mysql"
//  val username: String = "root"
//  val password: String = ""


  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT, worldTime TEXT, queuePosition SMALLINT )")
  }

  override def clearQueue(): Unit = {
    val statement = connection.createStatement()
    statement.execute("DROP TABLE IF EXISTS queue")
    setupTable()
  }


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?, ?, ?)")

    statement.setString(1, student.username)
    statement.setLong(2, student.timestamp)
    statement.setString(3, student.worldTime)
    statement.setInt(4, student.queuePosition)

    statement.execute()
  }

  override def updateStudentPosition(): Unit = {
    val statement = connection.prepareStatement("UPDATE queue SET queuePosition = queuePosition - ?")

    statement.setInt(1, 1)

    statement.execute()
  }

  override def studentLeftUpdatePosition(leftPosition: Int): Unit = {
    val statement = connection.prepareStatement("SELECT * FROM queue")
    val result: ResultSet = statement.executeQuery()

    while(result.next()){
      val position: Int = result.getInt("queuePosition")
      val username: String = result.getString("username")
      if(position > leftPosition) {
        val statement = connection.prepareStatement("UPDATE queue SET queuePosition = queuePosition - ? WHERE username= ?")
        statement.setInt(1, 1)
        statement.setString(2, username)
        statement.execute()
      }
    }
  }

  override def getStudent(studentName: String): StudentInQueue = {
    val statement = connection.prepareStatement("SELECT * FROM queue")
    val result: ResultSet = statement.executeQuery()

    while(result.next()) {
      val username: String = result.getString("username")
      if(username == studentName) {
        val timestamp: Long = result.getLong("timestamp")
        val worldTime: String = result.getString("worldTime")
        val queuePosition: Int = result.getInt("queuePosition")

        return new StudentInQueue(username, timestamp, worldTime, queuePosition)
      }
    }
    //not a fan of having it return this, but it should never get to this point anyways
    println("Could not find ", studentName + "'s data")
    new StudentInQueue("could not find user data", 0, "check getStudent", -5)
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
      val worldTime = result.getString("worldTime")
      val queuePosition = result.getInt("queuePosition")
      queue = new StudentInQueue(username, timestamp, worldTime, queuePosition) :: queue
    }

    queue.reverse
  }

}
