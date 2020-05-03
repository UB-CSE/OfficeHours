package model.database

import java.sql.{Connection, DriverManager, ResultSet}

import model.StudentInQueue


class Database extends DatabaseAPI{

//  val url = "jdbc:mysql://mysql/officehours?autoReconnect=true"
//  val username: String = sys.env("DB_USERNAME")
//  val password: String = sys.env("DB_PASSWORD")

  val url="jdbc:mysql://localhost/mysql?serverTimezone=UTC"
  val username: String = "root"
  val password: String = "12345678"

  var connection: Connection = DriverManager.getConnection(url, username, password)
  setupTable()


  def setupTable(): Unit = {
    val statement = connection.createStatement()
    statement.execute("CREATE TABLE IF NOT EXISTS queue(username TEXT, timestamp BIGINT, topic TEXT, subtopic TEXT)")
    statement.execute("CREATE TABLE IF NOT EXISTS TaStats(username TEXT, numOfStudentsHelped INT)")
  }


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    val statement = connection.prepareStatement("INSERT INTO queue VALUES (?,?,?,?)")

    statement.setString(1, student.username)
    statement.setLong(2, student.timestamp)
    statement.setString(3, student.topic)
    statement.setString(4, student.subtopic)

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
      val topic = result.getString("topic")
      val subtopic = result.getString("subtopic")
      queue = new StudentInQueue(username, timestamp,topic,subtopic) :: queue
    }
    queue.reverse
  }

  override def addTA(username:String):Unit={
    var statement = connection.prepareStatement("SELECT * FROM TaStats")
    val result: ResultSet = statement.executeQuery()
    var nameOfTA:List[String]=List()

    while (result.next()) {
      val username2 = result.getString("username")
      println(username2)
      nameOfTA=nameOfTA:+username2
    }

    if(!nameOfTA.contains(username)){
      println("Adding")
      statement = connection.prepareStatement("INSERT INTO TaStats VALUES (?,?)")
      statement.setString(1, username)
      statement.setInt(2, 0)

      statement.execute()
    }
    else{
      println("Already in the database")
    }

    statement.execute()
  }

   override def addStudentHelped(usernameTA:String): Unit ={
     println(usernameTA)
     var statement = connection.prepareStatement("SELECT*FROM TaStats WHERE username=?")
     statement.setString(1, usernameTA)
     val result: ResultSet = statement.executeQuery()
     if(result.first()){
       val num:Int=result.getInt(2)
       val updatedNum=num+1
       println(result)
       println(updatedNum)

       statement = connection.prepareStatement("UPDATE TaStats SET numOfStudentsHelped=numOfStudentsHelped+1 WHERE username = ?")
       statement.setString(1, usernameTA)
     }
     statement.execute()
   }

}
