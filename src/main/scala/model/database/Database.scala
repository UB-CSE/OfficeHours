package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue
import com.github.t3hnar.bcrypt._

class Database extends DatabaseAPI{

  val url = "jdbc:mysql://localhost/mysql?serverTimezone=UTC"
  val username: String = "root"//sys.env("DB_USERNAME")
  val password: String = "Dragonfriend00!"//sys.env("DB_PASSWORD")

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue (username TEXT, timestamp BIGINT)")
    //creates table for storing username and passwords or registered people and storing the salt for there password
    statement.execute("CREATE TABLE IF NOT EXISTS authentication (username TEXT, password TEXT, salt TEXT)")
    //makes username "" invalid for registration
    val statement2 = connection.prepareStatement("SELECT * FROM authentication WHERE username = ?")
    statement2.setString(1, "")
    val result = statement2.executeQuery()
    if(!result.next()) {
      val statement3 = connection.prepareStatement("INSERT INTO authentication VALUES (?,?,?)")
      statement3.setString(1, "")
      statement3.setString(2, "")
      statement3.setString(3, "")
      statement3.execute()
    }
  }

  override def addUserToAuthenticate(username: String, hashpass: String, salt: String): Boolean = {
    //gets user if they exist in database
    val result = connection.prepareStatement("SELECT * FROM authentication WHERE username=?")
    result.setString(1, username)
    val result2 = result.executeQuery()
    //checks to make sure user does not exist

    if(!result2.next()) {
     //adds user to database
     val statement = connection.prepareStatement("INSERT INTO authentication VALUE (?, ?, ?)")

     statement.setString(1, username)
     statement.setString(2, hashpass)
     statement.setString(3, salt)

     statement.execute()
     true
   }
   else
     //tell server that account exists already
     false
  }

  override def authenticate(username: String, pass: String): String = {
    //gets data if the student exists
    val result = connection.prepareStatement("SELECT * FROM authentication WHERE username=?")
    result.setString(1, username)
    val result2 = result.executeQuery()
    // checks if student exits
    if(username == "") {
      "bad user"
    }
    else if(result2.next()) {
      //hashes password and checks if it is correct based on the salt
      val hashpass = result2.getString("password")
      val salt = result2.getString("salt")
      if(pass.bcrypt(salt) == hashpass) {
        //sends string back to tell that password was correct
        "logged in"
      }
      else
        // sends string back to tell that password was wrong
        "bad pass"
    }
    else
      // tells server the account does not exist
      "DNE"
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
