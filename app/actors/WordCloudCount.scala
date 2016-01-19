package actors


import actors.WordCloudCount.GetCloudCount
import akka.actor.{Props, ActorRef, Status, Actor}
import akka.actor.Actor.Receive
import business.WordCount
import db.MysqlSlick
import models.TwitterUser
import play.api.Configuration
import play.cache.Cache

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Success}
import business.WordCount._
import play.api.cache.CacheApi
import play.api.Play.current

/**
  * Created by prabhitharajidi on 1/18/16.
  */
object WordCloudCount {

  implicit val config= play.api.Play.configuration
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  def props = Props(classOf[WordCloudCount], config,ec)
  case class GetCloudCount()

}

class WordCloudCount(implicit config: Configuration, implicit val ec: ExecutionContext) extends Actor {

  val store: MysqlSlick = new MysqlSlick("mysql")

  def getCloudCount(actorRef: ActorRef): Future[Map[String,Int]] =
  {
    println("test")
     WordCount.getWordCountForCloudInAllTweets("cloud")(ec,store)

  }

  override def receive = {
    case GetCloudCount() =>
      println(" in cloud count")
      val futureSender = sender()
      val allWordsCount = getCloudCount(sender())

      allWordsCount onComplete
        {
          case Success(e : Map[String,Int]) =>

            futureSender ! e
          case Failure(error) =>
            futureSender.tell(new Status.Failure(error), self)
        }
  }

}
