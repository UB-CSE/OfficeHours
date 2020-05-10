package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.{Staff, StudentInQueue}


class Database extends DatabaseAPI{
  //val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = "root"
  val password: String = sys.env("DB_ROOT2")
  val url = "jdbc:mysql://localhost:3306/office_hours"
  //val username: String = sys.env("DB_USERNAME")
  //val password: String = sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()
  setupAdmin()



  def setupTable(): Unit = {
    val statement = connection.createStatement()
    //statement.execute("DROP TABLE queue")
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, connectionTime TEXT)")
    statement.execute("CREATE TABLE if NOT EXISTS staff (ubit TEXT, password TEXT)")

  }

  def setupAdmin(): Unit = {
    val statement = connection.prepareStatement("INSERT IGNORE INTO staff VALUE (?,?)")

    statement.setString(1, "serveradmin")
    statement.setString(2,"adminpassword1")

    statement.execute()
  }

  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?)")

    statement.setString(1, student.username)
    statement.setString(2, student.timestamp)

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
      val timestamp = result.getString("connectionTime")
      queue = new StudentInQueue(username, timestamp) :: queue
    }

    queue.reverse
  }

  override def getStaff: List[Staff] = {
    val statement = connection.prepareStatement("SELECT DISTINCT * FROM staff")
    val result: ResultSet = statement.executeQuery()

    var staffList: List[Staff] = List()

    while(result.next()){
      val ubit= result.getString("ubit")
      val password = result.getString("password")
      staffList = new Staff(ubit, password) :: staffList
    }
    staffList
  }

  override def addStaff(staff: Staff): Unit = {
    val statement = connection.prepareStatement("INSERT IGNORE INTO staff VALUE (?,?)")

    statement.setString(1, staff.ubit)
    statement.setString(2,staff.password)

    statement.execute()
  }
}
