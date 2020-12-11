package main.scala.model



//@@@@@@@@@---(project contribution)
//So this puts a limit on the time passed by for each user.
object newFunctionality {

  def eligibility (username: String): Boolean = {

    //update and remove ready to join users from banned list whenever a previous user or new user attempts to join.
    var tempNewList: List[String] = List()
    for (user <-  records.banned){
      if ((System.nanoTime() - records.usernameToLastTime(user)) / ((10 * 10 * 10 * 10 * 10 * 10 * 10 * 10 * 10)) < 10) {
        tempNewList = user::tempNewList
      }
    }
    records.banned = tempNewList




    if (records.previousUsernames.contains(username)) { //if it's the user's second time or more, go thru this, if not, then do else.
      var timeWentBy: Long = (System.nanoTime() - records.usernameToLastTime(username)) / ((10 * 10 * 10 * 10 * 10 * 10 * 10 * 10 * 10)) //This is just a conversion from nanoSeconds to seconds. So for clarity, we'll have time be in seconds when comparing to our expected time.    //println(System.nanoTime() - server.usernameToLastTime(username))    println(timeWentBy)
      if (timeWentBy > 10) { //If the time since entering queue previously is less than the time limit, then enter, if not, wait more. To speed up, we'll wait 60 seconds only. But you may make a time limit of 3 hours, or even 24 hours(1 day), or even longer.

        records.usernameToLastTime = records.usernameToLastTime + (username -> System.nanoTime()) //update their new time since they're entering now.
        records.banned =  username::records.banned
        true //return true to allow entrance since enough time went by.

      }
      else {
        false //don't allow entrance, and don't update time since they didn't enter in queue, and continue based on previous entrance time.
      }
    }


    else{ //all new usernames joined will be added to this list. It's mainly used when deriving previous time in upper code. Used to check if students are a key in the mapping usernameToLastTime.
      records.previousUsernames = username::records.previousUsernames
      records.usernameToLastTime = records.usernameToLastTime + (username -> System.nanoTime()) //also save their timing. & return true to allow entrance to queue.

      records.banned = username::records.banned

      true
    }



    //All in all, if students first time in, no time is checked, and they get added to usernames list, and to their time mapping. If when second time in or more (determined if they're in list), then they will be checked by their time. If eligible, they proceed to get helped, and their username remains in username list forever, and update to their new last time in. If not eligible, then the time went by will increase until they can enter.
  }


}

object records {
  var previousUsernames: List[String] = List()
  var usernameToLastTime: Map[String, Long] = Map()


  var banned: List[String] = List()
}

//note that to simplify testing, I made an easy API that contains all the functionality of the new feature in within here, so it's independent from the server, and the server and it's event handlers won't be needed in the testing. because it's harder to send messages and test the server form unit testing.
//This new method will be called by the EnterQueueListener event handler, and the API is to just return true of false, based on entrance ability. The only new thing that isn't tested is the event handler deciding to continue or not based on true or false returned from this methods call. Which is simple.
//Also note that whenever a user joins, the EnterQueueListener always run. And that implies that this method will always run. And note that it should be having the necessary variables, such as previousUsernames, or usernameToLastTime, within the server itself. But since we're keeping the server out of the testing. And this functionality will be called by the servers EnterQueueListener with passing on any relevant data, then we can store the variables data structures outside of the server. And again, the server doesn't need these since it didn't have them before, and and it's not part of the API, and it's only necessary for us. And besides, since they're here, and are here within the file, then the server may access them if necessary.
//So you may go ahead and persist these w/ a DB or a file, or do what you want you further.