package tests
import main.scala.model
import org.scalatest._

class testingOfficeHoursLimiter extends FunSuite {

  test("test one person") {

    var elegibility: Boolean = model.newFunctionality.eligibility("Nofal")
    assert(elegibility) //this is like the equivalent of when the server's listener runs, adding the person to the queue.

    assert(!model.newFunctionality.eligibility("Nofal"))

    Thread.sleep(11000) //11 seconds to wait.
    //Note that it will initiate the count to 10 seconds, (not really count, but only record change in time, and if it's greater than 10). But since code takes some time to run, then we need to test at least after 11 seconds to see.
    //Note that exact precision down to the billionth of a second is not necessary. All we care about is that it waited around 10 seconds, and not 2 seconds or 3 hours. Besides, the program in real life will have a timing of about several hours, but we want to go faster in here. And the exact seconds won't matter. And a few seconds off aren't noticeable by humans. And also it won't be a coincidence that a student is wanting to join at the exact time he think's he's able to.

    assert(model.newFunctionality.eligibility("Nofal"))




    model.records.previousUsernames = List()
    model.records.usernameToLastTime = Map()
    //note that since the object records permanently saves, when going from one test to another. And we depend that the object is fresh new, then we need to clear. Or we can also sleep before transiting, or we can also use new usernames.
    //the records object behavior is not important and has undefined behavior, that we could use lead to. Normally, from the servers side currently, on a single run, the object gets filled with users and never gets cleared, and we may persist it occasionally. And when the server restarts, that'll be the only time it clears.
  }


  test("test more than one person") {

    assert(model.newFunctionality.eligibility("Nofal"))
    assert(model.newFunctionality.eligibility("Liam")) //So all of these codes like this just initiates the counting, and returns a true or false.
    assert(model.newFunctionality.eligibility("Sophia"))

    assert(!model.newFunctionality.eligibility("Nofal"))
    assert(!model.newFunctionality.eligibility("Liam"))
    assert(!model.newFunctionality.eligibility("Sophia"))

    Thread.sleep(11000)

    assert(model.newFunctionality.eligibility("Nofal"))
    assert(model.newFunctionality.eligibility("Liam"))
    assert(model.newFunctionality.eligibility("Sophia"))

    model.records.previousUsernames = List()
    model.records.usernameToLastTime = Map()
  }


  test("test more than one person, mixed times") {

    assert(model.newFunctionality.eligibility("Nofal"))
    Thread.sleep(3000)
    assert(model.newFunctionality.eligibility("Liam"))
    Thread.sleep(3000)
    assert(model.newFunctionality.eligibility("Sophia"))

    assert(!model.newFunctionality.eligibility("Nofal"))
    assert(!model.newFunctionality.eligibility("Liam"))
    assert(!model.newFunctionality.eligibility("Sophia"))


    Thread.sleep(5000)
    assert(model.newFunctionality.eligibility("Nofal")) //it's been 10 seconds for Nofle
    assert(!model.newFunctionality.eligibility("Liam"))
    assert(!model.newFunctionality.eligibility("Sophia"))

    Thread.sleep(3000)
    assert(model.newFunctionality.eligibility("Liam")) //good...
    assert(!model.newFunctionality.eligibility("Sophia"))

    Thread.sleep(3000)
    assert(model.newFunctionality.eligibility("Sophia")) //good...


    model.records.previousUsernames = List()
    model.records.usernameToLastTime = Map()
  }


  test("test more than one person, with targetting") {

    assert(model.newFunctionality.eligibility("Nofal"))
    assert(model.newFunctionality.eligibility("Liam"))
    assert(model.newFunctionality.eligibility("Sophia"))
    Thread.sleep(11000)


    assert(model.newFunctionality.eligibility("Liam"))

    assert(model.newFunctionality.eligibility("Nofal"))
    assert(!model.newFunctionality.eligibility("Liam")) //Liam was the one who entered queue, and not others.
    assert(model.newFunctionality.eligibility("Sophia"))

    Thread.sleep(11000)
    assert(model.newFunctionality.eligibility("Nofal"))
    assert(model.newFunctionality.eligibility("Liam"))
    assert(model.newFunctionality.eligibility("Sophia"))



    model.records.previousUsernames = List()
    model.records.usernameToLastTime = Map()
  }

}
