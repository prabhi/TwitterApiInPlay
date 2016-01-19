package db

import scala.slick.driver._

/**
  * Created by prabhitharajidi on 1/18/16.
  */
trait DriverSlick{
  val driver: JdbcProfile
}

