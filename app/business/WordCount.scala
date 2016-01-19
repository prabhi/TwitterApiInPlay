package business


import db.{TweetTransformer, MysqlSlick}

import scala.collection.immutable.HashMap
import scala.concurrent.{ExecutionContext, Promise, Future}
import scala.util.{Failure,Success}

/**
  * Created by prabhitharajidi on 1/18/16.
  */
object WordCount {


  def getWordCountForCloudInAllTweets(wordToSearch: String)(implicit ec: ExecutionContext, store: MysqlSlick): Future[Map[String,Int]] = {

    val promise = Promise[Map[String,Int]]()
    val tweetTransformer = new TweetTransformer(store)
    val hashMap = collection.mutable.Map[String, Int]().withDefaultValue(0)

    val futureTweets: Future[Seq[String]] = tweetTransformer.getTweets()

    futureTweets onComplete {
      case Success(seqOfTweets: Seq[String]) =>
        for(i <-  seqOfTweets)
          {
            if(!i.isEmpty) {
             val cloudValue= i.split(" ").filter(t=> t.contains(wordToSearch)).size
              hashMap(wordToSearch) +=cloudValue
            }

          }
        promise.success(hashMap.toMap)
        //need to use aggregator pattern to actually aggregate the words and their counts.
        //for now just doing a simple split and find



      case Failure(error) => promise.failure(error)
    }

    promise.future
  }
}