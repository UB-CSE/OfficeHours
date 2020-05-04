package model.database

import model.StudentInQueue
import com.github.t3hnar.bcrypt._

class TestingDatabase extends DatabaseAPI {

  var data: List[StudentInQueue] = List()


  override def addStudentToQueue(student: StudentInQueue): Unit = {
    data ::= student
  }


  override def removeStudentFromQueue(username: String): Unit = {
    data = data.filter(_.username != username)
  }


  override def getQueue: List[StudentInQueue] = {
    data.reverse
  }
  //tests general logic
  var listOfUsers: Map[String, List[String]] = Map()
  override def addUserToAuthenticate(username: String, hashpass: String, salt: String): Boolean = {
    if(listOfUsers.contains(username)) {
      false
    }
    else {
      listOfUsers += (username -> List(hashpass, salt))
      true
    }
  }

  override def authenticate(username: String, pass: String): String = {

    if(username == "") {
      "bad user"
    }
    else if(listOfUsers.contains(username)) {
      //hashes password and checks if it is correct based on the salt
      val hashpass = listOfUsers(username)(0)
      val salt = listOfUsers(username)(1)
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
}
