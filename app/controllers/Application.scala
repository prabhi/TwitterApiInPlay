package controllers

import actors.{WordCloudCount, GetUserInfo}
import actors.GetUserInfo.GetUserBio
import actors.WordCloudCount.GetCloudCount
import akka.util.Timeout
import db.MysqlSlick
import models.{TwitterScreenNames, TwitterUser}
import play.api.Play
import play.api.cache.CacheApi
import play.api.mvc._
import client.TwitterApiClient

import akka.actor._
import javax.inject._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import akka.pattern.ask
import play.api.libs.json.{JsValue, Json}
import models.PlayJsonFormat._
import play.api.Play.current



@Singleton
class Application @Inject() (system: ActorSystem) extends Controller {



  val userActor = system.actorOf(GetUserInfo.props, "getUserActor")
  val countActor = system.actorOf(WordCloudCount.props,"wordCountActor")

  implicit val timeout = Timeout(5.minutes)

  def getTwitterUsers = Action  {
    request =>

      val json = request.body.asJson.get
      val twitterHandles = json.as[TwitterScreenNames]
      Ok(Json.toJson(twitterHandles))

  }

  def getWordCount =  Action.async {

      (countActor ? GetCloudCount).mapTo[Map[String,Int]].map { r =>

        Ok(Json.toJson(r))
      }
  }




  def getTwitterUsersBio = Action.async {
    request =>

      val json = request.body.asJson.get

      println(json.toString())
      val twitterHandles = json.as[TwitterScreenNames]

      println("twitterhandles =" + twitterHandles)

    (userActor ? GetUserBio(twitterHandles)).mapTo[Seq[TwitterUser]].map { r =>

      Ok(Json.toJson(r))
    } recover {
      case e: NoSuchElementException => NotFound(s"no data found")
      case t: Throwable => BadRequest(t.getMessage)
      case ex: Exception => BadRequest(ex.getMessage)
    }

  }


}