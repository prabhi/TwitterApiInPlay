package client

import akka.actor.{ActorSystem, ActorRef, Actor}
import models.{TwitterUser, TwitterToken}
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{Json, JsValue}
import spray.httpx.encoding.{Deflate, Gzip}

import scala.concurrent.{Promise, Future}
import scala.util.{Failure, Success}
import spray.client.pipelining._
import spray.http.{FormData, HttpRequest, HttpResponse}
import spray.json._
import org.apache.commons.codec.binary.Base64
import play.api.Logger


class TwitterApiClient(implicit conf: Configuration) {


  def getBearerToken(implicit actorRef: ActorSystem): Future[TwitterToken] = {

    // execution context for the future

    import spray.httpx.SprayJsonSupport._
    import models.TwitterJsonProtocol._

    val twitterConsumerKey = conf.getString("TwitterConfig.consumerKey");
    val twitterConsumerSecret = conf.getString("TwitterConfig.consumerSecret")


    val customerData = twitterConsumerKey.get + ":" + twitterConsumerSecret.get
    val twitterOAuthUrl = conf.getString("TwitterConfig.oAuth.url");


    // setup request/response logging
    val logRequest: HttpRequest => HttpRequest = { r => Logger.debug(r.toString); r}
    val logResponse: HttpResponse => HttpResponse = { res => Logger.debug(res.toString); res}

    val authKey = new String(Base64.encodeBase64(customerData.getBytes("UTF-8")))

    Logger.debug("authkey=   "+ authKey)

    val pipeline: HttpRequest => Future[TwitterToken] = (
      addHeader("Authorization", s"Basic $authKey")
        ~> addHeader("Content-Type", "application/x-www-form-urlencoded")
        ~>logRequest
        ~> sendReceive
        ~>logResponse
        ~> unmarshal[TwitterToken]
      )

    val response = pipeline {
      Post(twitterOAuthUrl.get, FormData(Map("grant_type" -> "client_credentials")))
    }

    response
  }

  def getUserInfo(implicit actorRef: ActorSystem,tokenStr: String ,userScreenName: String) : Future[TwitterUser] =
  {
    val promise = Promise[TwitterUser]()
    import spray.httpx.SprayJsonSupport._
    import models.TwitterJsonProtocol._



    val twitterUserShowUrl = conf.getString("TwitterConfig.userShow.url").get+"?screen_name="+userScreenName;

    val logRequest: HttpRequest => HttpRequest = { r => Logger.debug(r.toString); r}
    val logResponse: HttpResponse => HttpResponse = { res => Logger.debug(res.toString); res}



    val pipeline: HttpRequest => Future[HttpResponse] = (
      addHeader("Authorization", s"$tokenStr")
        ~> addHeader("Content-Type", "application/json")
        ~>logRequest
        ~> sendReceive
        ~>logResponse

      )

    val response = pipeline {
      Get(twitterUserShowUrl)
    }

    response map { r =>
      r.status.intValue match {
        case 200 =>
          val responseUmmarshaller= unmarshal[TwitterUser]
          promise.success(responseUmmarshaller.apply(r))

        case _ => promise.failure(new Exception(r.message.toString))
      }
    }

    promise.future
  }
}
