package db

import scala.slick.driver

/**
  * Created by prabhitharajidi on 1/18/16.
  */
trait Tweets { this: DriverSlick =>
  import driver.api._

  class TweetsTable(tag: Tag) extends Table[TweetRecord](tag, "tweets") {

    def id = column[Int]("idtweets", O.PrimaryKey)

    def tweetText = column[String]("tweetText")

    def userId = column[Int]("userId")


    def * = (id, tweetText, userId) <> ((TweetRecord.apply _).tupled, TweetRecord.unapply)
  }

  case class TweetRecord(
                         id: Int,
                         tweetText: String,
                         userId: Int)

  val tweetTableQuery = TableQuery[TweetsTable]


}
