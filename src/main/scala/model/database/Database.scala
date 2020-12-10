package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.{Register, RegisterUser, StudentInQueue}


class Database extends DatabaseAPI{

  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
  val username: String = sys.env("DB_USERNAME")
  val password: String = sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    //Schedule an appointment
    statement.execute("CREATE TABLE IF NOT EXISTS queue (email TEXT, kindOfUser TEXT, timestamp BIGINT)")
    //Register new user into DB
    statement.execute("CREATE TABLE IF NOT EXISTS register (fullname TEXT, username TEXT, email TEXT, password TEXT, kindOfUser TEXT, id INT PRIMARY KEY AUTO_INCREMENT)")
  }


  //Add student into database
  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUE (?, ?, ?)")

    statement.setString(1, student.email)
    statement.setString(2, student.kind)
    statement.setLong(3, student.timestamp)

    statement.execute()
  }

  //Register a new user into db
  override def register(user: RegisterUser): Unit = {
    val statement = connection.prepareStatement("INSERT INTO register VALUE (?, ?, ?, ?, ? ?)")

    statement.setString(1, user.fullName)
    statement.setString(2, user.username)
    statement.setString(3, user.email)
    statement.setString(4, user.password)
    statement.setString(5, user.kind)

    statement.execute()
  }



  override def removeStudentFromQueue(email: String): Unit = {
    val statement = connection.prepareStatement("DELETE FROM queue WHERE email=?")

    statement.setString(1, email)

    statement.execute()
  }


  override def getQueue: List[StudentInQueue] = {
    val statement = connection.prepareStatement("SELECT * FROM queue") //Get everything from DB
    val result: ResultSet = statement.executeQuery()

    var queue: List[StudentInQueue] = List()

    while (result.next()) {
      val email = result.getString("email")
      val kind = result.getString("kindOfUser")
      val timestamp = result.getLong("timestamp")
      queue = new StudentInQueue(email, kind, timestamp) :: queue
    }

    queue.reverse
  }

  override def getUserFromDB : List[RegisterUser] = {
    //Get everything from DB
    val statement = connection.prepareStatement("SELECT * FROM register")
    val result: ResultSet = statement.executeQuery()

    var queue: List[RegisterUser] = List()

    while (result.next()) {
      val fullName = result.getString("fullName")
      val username = result.getString("username")
      val email = result.getString("email")
      val password = result.getString("password")
      val kind = result.getString("kindOfUser")
      val id = result.getInt("id")
      queue = new RegisterUser(fullName, username, email, password, kind, id) :: queue
    }

    queue.reverse
  }

  override def login(email: String, password: String): Boolean = {
    true
  }

}
