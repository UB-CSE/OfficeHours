package helper

import scala.io.Source
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

object Dotenv {
  // To load .env file to the Environmental Variables for development
  def loadEnv (envFilePath: String = ".env"): Unit = {
    var envLines: List[String] = Try(Source.fromResource(envFilePath).getLines().toList) match {
      case Success(lines) => lines
      case Failure(e) => {
        print("[Warn] Failed to load .env: ")
        println(e)

        // return empty list if .env not exist
        List()
      }
    }

    for (line <- envLines) {
      // skip the lines which are commented out
      if (line.trim != "" && !line.trim.startsWith("#")) {
        // Split the line by character `=` once
        var lineContent: Array[String] = line.split("=", 2)
        if (lineContent.length > 0) {
          if (lineContent.length == 2) {
            DirtyEnvironmentHack.setEnv((sys.env ++ Map(
              lineContent(0) -> lineContent(1)
            )).asJava)
          }
        }
      }
    }
  }
}

