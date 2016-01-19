package db

import slick.dbio.Effect
import slick.driver.MySQLDriver
import slick.driver.MySQLDriver.api._
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{GetResult, SQLActionBuilder}
import slick.profile.SqlStreamingAction
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

class MysqlSlick(conf: String){
  val driver   = MySQLDriver
  val database = Database.forConfig(conf)

  def executeCommand[A](command: String)(implicit rtype: GetResult[A], ec: ExecutionContext): Future[Vector[A]] = {

    val sqlAction: SQLActionBuilder = sql"#$command"
    val action: SqlStreamingAction[Vector[A], A,Effect] = sqlAction.as[A]

    val f: Future[Vector[A]] = database.run(action)

    f
  }
}

