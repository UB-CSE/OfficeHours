package model

import scala.util.{Failure, Success, Try}

object Configuration {

  val DEV_MODE = true

  /**
   * DB_TYPE
   *
   * Type of the database the server gonna use
   * Types are implemented in model.database
   *
   * "MySQL" || "List"
   *
   * default: "List"
   * */
  var DB_TYPE: String = Try(sys.env("DB_TYPE")) match {
    case Success(dbType) => dbType
    case Failure(e) => "List"
  }

}
