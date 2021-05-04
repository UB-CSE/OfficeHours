package model

import play.api.libs.json.{JsValue, Json}

import scala.annotation.tailrec


object StudentInQueue {


  def cleanString(input: String): String = {
    var output = input
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
    if (output.length > 20) {
      output = output.slice(0, 20) + "..."
    }
    output
  }

  def apply(username: String, helpDescription: String, timestamp: Long): StudentInQueue = {
    new StudentInQueue(cleanString(username), cleanString(helpDescription), timestamp)
  }

  //Below this line is my contribution to the project
  //The purpose of this code is to remove any banned words, ie cuss words or whatever the moderators decide
  //is unacceptable to be in a question
  //The method utilizes recursion (LO3) to parse through each word in the help message and filter out banned words
  //Params are the message as a String and a list of Strings, with each being a banned word
  //I have also tested this method in the tests package under "MyProjectContribution"

  //------------------------------------------------------------------------------------------------------------------
  def bannedWordFilter(helpDescription:String, bannedWords: List[String]): String = {

    //removes commas to fix issue where commas would allow words to pass
    val newHelpDescription: String = helpDescription.replace(",", "")
    val splitOnSpace: Array[String] = newHelpDescription.split(" ")

    val retString: String = bannedWordFilterHelper(splitOnSpace,bannedWords,splitOnSpace.length)

    retString

  }

  @tailrec
  def bannedWordFilterHelper(splitMessage: Array[String], bannedWords: List[String], messageLength: Int): String = {

    if(messageLength == 0){
      val string = splitMessage.mkString(" ")
      string

    }else{
      val thisWord: String = splitMessage(messageLength - 1)

      if(bannedWords.contains(thisWord)){

        val newSplitMessage: Array[String] = splitMessage.patch(splitMessage.indexOf(thisWord), Seq("CENSORED"), 1)
        bannedWordFilterHelper(newSplitMessage, bannedWords: List[String], messageLength-1)

      }else{
        bannedWordFilterHelper(splitMessage, bannedWords: List[String], messageLength - 1)
      }
    }
  }

  //------------------------------------------------------------------------------------------------------------------

}

class StudentInQueue(val username: String, val helpDescription: String, val timestamp: Long) {

  def asJsValue(): JsValue ={
    val messageMap: Map[String, JsValue] = Map(
      "username" -> Json.toJson(username),
      "helpDescription" -> Json.toJson(helpDescription),
      "timestamp" -> Json.toJson(timestamp)
    )
    Json.toJson(messageMap)
  }

}
