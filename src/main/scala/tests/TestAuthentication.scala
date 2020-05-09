package tests


import org.scalatest.FunSuite
import com.github.t3hnar.bcrypt._
import model.database.TestingDatabase

class TestAuthentication extends FunSuite {
  val database = new TestingDatabase


  //resets database for testing purposes


  test("tests to see if authentication works as intended") {
    //in testDB it is possible to have username="" but not in the authenticate method
    //authenticate does not check for valid hashed password or salt, useful for testing less power on the system
    val test1 = database.authenticate("username", "password")

    assert(test1 == "DNE", "test1 " + test1)
    val test2Salt: String = generateSalt
    val test2 = database.addUserToAuthenticate("username", "password".bcrypt(test2Salt),test2Salt)

    assert(test2, "test2 is false")

    val test3 = database.authenticate("username", "password")

    assert(test3 == "logged in", "test3 " + test3)

    val test4 = database.authenticate("username", "pass")

    assert(test4 == "bad pass", "test4 " + test4)

    val test5 = database.authenticate("username", "")

    assert(test5 == "bad pass", "test5 " + test5)


    val test6 = database.authenticate("","")

    assert(test6 == "bad user", "test6 " + test6)
    val test7Salt = generateSalt
    val test7 = database.addUserToAuthenticate("username","password".bcrypt(test7Salt),test7Salt)

    assert(!test7, "test7 is true")
  }


}
