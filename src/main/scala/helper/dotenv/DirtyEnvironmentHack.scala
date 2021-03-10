package helper.dotenv

import java.util.Collections
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
 * Rewrite the runtime Environment, embedding entries from the .env file.
 *
 * Taken from: https://github.com/mefellows/sbt-dotenv/blob/master/src/main/scala/au/com/onegeek/sbtdotenv/DirtyEnvironmentHack.scala
 * Original from: http://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java/7201825#7201825
 *
 * Created by mfellows on 20/07/2014.
 */
object DirtyEnvironmentHack {
  def setEnv(newEnv: java.util.Map[String, String]): Unit = {
    Try({
      val processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment")

      val theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment")
      theEnvironmentField.setAccessible(true)
      val env = theEnvironmentField.get(null).asInstanceOf[java.util.Map[String, String]] // scalastyle:off null
      env.putAll(newEnv)

      val theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment")
      theCaseInsensitiveEnvironmentField.setAccessible(true)
      val ciEnv = theCaseInsensitiveEnvironmentField.get(null).asInstanceOf[java.util.Map[String, String]] // scalastyle:off null
      ciEnv.putAll(newEnv)
    }) match {
      case Failure(_: NoSuchFieldException) =>
        Try({
          val classes = classOf[Collections].getDeclaredClasses
          val env = System.getenv
          classes.filter(_.getName == "java.util.Collections$UnmodifiableMap").foreach(cl => {
            val field = cl.getDeclaredField("m")
            field.setAccessible(true)
            val map = field.get(env).asInstanceOf[java.util.Map[String, String]]
            map.clear()
            map.putAll(newEnv)
          })
        }) match {
          case Failure(NonFatal(e2)) =>
            e2.printStackTrace()
          case Success(_) =>
        }
      case Failure(NonFatal(e1)) =>
        e1.printStackTrace()
      case Success(_) =>
    }
  }
}
