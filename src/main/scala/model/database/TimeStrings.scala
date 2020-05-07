package model.database

import java.util.Calendar

object TimeStrings {
  //get current hour and minute, offsets can be used as a timer or to change into a different timezone
  def getDateTime(minuteOffset:Int=0,HourOffset:Int=0):String = {
    var date = Calendar.getInstance()

    var minutes_without_correction = date.get(Calendar.MINUTE)+minuteOffset

    var Minute = (minutes_without_correction%60).toString

    var minuteCarryOver = ((minutes_without_correction)/60).toInt

    var Hour = ((date.get(Calendar.HOUR_OF_DAY)+HourOffset+minuteCarryOver)%24)

    var PMAM = "AM"

    if(Hour>12){
      PMAM = "PM"
      Hour=Hour%12
    }
    if(Minute.length<2){
      Minute="0"+Minute
    }
    Hour.toString+":" + Minute + " " + PMAM
  }
    //get wait time in minutes
  def getWaitTime(usersAheadInLine:Int, averageWaitTime:Int = 10, extraTime:Int = 0): String ={//get wait time in minutes

    val estimatedTime= (usersAheadInLine*averageWaitTime)

    val extra = usersAheadInLine*extraTime//give the TAs extra time

    val finalTime = estimatedTime+extra

    finalTime.toString
  }

  def getOpeningTime(placeInLine:Int, averageWaitTime:Int = 10, extraTime:Int = 0): String ={
    val estimatedTime= (placeInLine*averageWaitTime)
    val extra = placeInLine*extraTime//give the TAs extra time
    val finalTime = estimatedTime+extra
    getDateTime(finalTime)
  }





}
