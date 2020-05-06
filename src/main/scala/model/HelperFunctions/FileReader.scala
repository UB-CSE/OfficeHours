package main.scala.model.HelperFunctions
import scala.io.{BufferedSource, Source}

object FileReader {

  /* Will read the file with the TA Passwords, to make it easy since I'm not doing a demo I made it with the names
   of the TA's that are listed on the piazza OH schedule*/

  def fileReader(filename: String, taName: String): Boolean = {
    var isTA = false
    val name=taName.toLowerCase
    val file: BufferedSource = Source.fromFile(filename)
      for (line <- file.getLines()) {
        val lowerLine=line.toLowerCase
        if (lowerLine == name) {
          isTA = true
        }
      }
    isTA
  }

  /*def main(args: Array[String]): Unit = {
    val filename="C:\\Users\\graci\\IdeaProjects\\OfficeHours\\src\\main\\scala\\model\\TAPasswords(names).txt"
    println(fileReader(filename, "Jon"))
    println(fileReader(filename, "jon"))
    println(fileReader(filename, "sally"))

  }*/
}
