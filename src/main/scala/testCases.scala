import java.sql.{Connection, DriverManager, ResultSet}
import model.database.{Database,DatabaseAPI,TestingDatabase}
import model.StudentInQueue
import play.api.libs.json.Json
import org.mindrot.jbcrypt.BCrypt
import org.scalatest.FunSuite

class testCases extends FunSuite{
    test("myTest"){

      val database = new TestingDatabase
      val student1 = new StudentInQueue("David","Project1",5)
      val student2 = new StudentInQueue("Alex","Project2",50)
      val student3 = new StudentInQueue("Jesse","Project3",500)
      val student4 = new StudentInQueue("Ethan","Project4",5000)
      val student5 = new StudentInQueue("Nathan","Project5",50000)


      database.addStudentToQueue(student1)
      database.addStudentToQueue(student2)
      database.addStudentToQueue(student3)
      val queue = database.getQueue
      assert(queue(0).username == "David")
      assert(queue(0).helpDescription == "Project1")
      assert(queue(0).timestamp == 5)

      assert(queue(1).username == "Alex")
      assert(queue(1).helpDescription == "Project2")
      assert(queue(1).timestamp == 50)

      assert(queue(2).username == "Jesse")
      assert(queue(2).helpDescription == "Project3")
      assert(queue(2).timestamp == 500)

      database.removeStudentFromQueue("Alex")
      val queue2 = database.getQueue
      assert(queue2(1).username == "Jesse")
      assert(queue2(1).helpDescription == "Project3")
      assert(queue2(1).timestamp == 500)

      database.clearQueue
      val queue3 = database.getQueue
      assert(queue3 == List())
      database.addStudentToQueue(student1)
      database.addStudentToQueue(student2)
      database.addStudentToQueue(student3)
      database.addStudentToQueue(student4)
      database.addStudentToQueue(student5)
      database.moveToEndOfQueue(student2)
      val queue4 = database.getQueue
      assert(queue4(0).username == "David")
      assert(queue4(0).helpDescription == "Project1")
      assert(queue4(0).timestamp == 5)
      assert(queue4(1).username == "Jesse")
      assert(queue4(1).helpDescription == "Project3")
      assert(queue4(1).timestamp == 500)
      assert(queue4(2).username == "Ethan")
      assert(queue4(2).helpDescription == "Project4")
      assert(queue4(2).timestamp == 5000)
      assert(queue4(3).username == "Nathan")
      assert(queue4(3).helpDescription == "Project5")
      assert(queue4(3).timestamp == 50000)
      assert(queue4(4).username == "Alex")
      assert(queue4(4).helpDescription == "Project2")
      assert(queue4(4).timestamp == 50001)




    }
}
