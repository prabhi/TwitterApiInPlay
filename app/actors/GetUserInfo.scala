package actors

import actors.GetUserInfo.GetUserBio
import akka.actor._
import client.TwitterApiClient
import models.{TwitterScreenNames, TwitterUser, TwitterToken}
import play.api.Play.current
import play.api.{Logger, Configuration}
import play.api.libs.json.Json
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Promise, Future}


import scala.util.control.Exception
import scala.util.{Success, Failure}



object GetUserInfo {

  implicit val config= play.api.Play.configuration
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  def props = Props(classOf[GetUserInfo], config,ec)
  case class GetUserBio(users: TwitterScreenNames)

}

class GetUserInfo(implicit config: Configuration, implicit val ec: ExecutionContext) extends Actor {

  import GetUserInfo._
  import context.dispatcher
  import context.system
  import spray.httpx.SprayJsonSupport._

  implicit val twitterApiClient = new TwitterApiClient()(config)

  def getBearerTokenString(actorRef: ActorRef)(implicit twitterApiClient: TwitterApiClient)  = {




    twitterApiClient.getBearerToken(context.system) onComplete
    {
      case Success(tokenStr: TwitterToken) => {

        actorRef ! "Bearer " + tokenStr.access_token
      }
      case Failure(e) => {
        actorRef ! e.getMessage
      }

    }



  }

  def getUserBio(actorRef: ActorRef,userList: TwitterScreenNames)(implicit twitterApiClient: TwitterApiClient): Future[Seq[TwitterUser]] = {

    val promise = Promise[Seq[TwitterUser]]

   val seqOfTwitterUsers = for{
     token <-twitterApiClient.getBearerToken(context.system)
     tokenStr = "Bearer "+token.access_token
   }yield {

     val futureList = Future.traverse(userList.twitterHandles.distinct)(i =>

       twitterApiClient.getUserInfo(context.system, tokenStr, i)
     )
     val listOfUsers = Await.ready({
       for {createdList <- futureList} yield {

         createdList
       }
     }, Duration.Inf)
     listOfUsers recover {
       case exception: Exception =>
         promise.failure(exception)
       case terminated: Terminated =>
         promise.failure(new Exception("An internal system error"))
     }
     listOfUsers
   }
    seqOfTwitterUsers.flatMap(t => t)

  }


  override def receive = {
    case GetUserBio(users) =>
      val futureSender = sender()
      val allTwitterUsers = getUserBio(sender(),users)

      allTwitterUsers onComplete
        {
          case Success(e : Seq[TwitterUser]) =>

            futureSender ! e
          case Failure(error) =>
            futureSender.tell(new Status.Failure(error), self)
        }
  }

}

