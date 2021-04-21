package tests

import org.scalatest.FunSuite
import model.StudentInQueue


class MyProjectContribution extends FunSuite {

  test("My Testing") {

    val testBannedList: List[String] = List("dog","cat","cow","hate")

    val testHelpMessage1: String = "I hate CSE116"
    val testHelpMessage2: String = "I love my dog and cat"
    val testHelpMessage3: String = "cat"
    val testHelpMessage4: String = "I love my dog, cow, and cat"


    assert(StudentInQueue.bannedWordFilter(testHelpMessage1,testBannedList) == "I CENSORED CSE116")
    assert(StudentInQueue.bannedWordFilter(testHelpMessage2,testBannedList) == "I love my CENSORED and CENSORED")
    assert(StudentInQueue.bannedWordFilter(testHelpMessage3,testBannedList) == "CENSORED")
    assert(StudentInQueue.bannedWordFilter(testHelpMessage4,testBannedList) == "I love my CENSORED CENSORED and CENSORED")



  }
}
