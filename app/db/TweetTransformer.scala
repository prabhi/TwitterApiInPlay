package db

import scala.concurrent.{Future, Promise, ExecutionContext}

/**
  * Created by prabhitharajidi on 1/18/16.
  */
class TweetTransformer (store: MysqlSlick) extends Tweets with DriverSlick {

  override val driver = store.driver

  import driver.api._


  def getTweets()(implicit ec: ExecutionContext) : Future[Seq[String]] = {
    val promise = Promise[Seq[String]]()

    println("in get tweets")

    val query = for {
      tweetRecord <- tweetTableQuery
    } yield tweetRecord.tweetText

    val a= query.result
    val futureResult: Future[Seq[String]] = store.database.run(a)

    futureResult map {
      result =>  promise.success(result)
        }

    futureResult recover
    {
      case(e: Exception) =>
        promise.failure(e)
    }

    promise.future
  }

}
