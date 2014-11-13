package actors

import akka.actor.{ActorLogging, Props, Actor, ActorRef}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue

import scala.concurrent.duration._

class UserActor(initialQuery:String, out:JsValue => Unit, tweetLoaderClient: ActorRef) extends Actor with ActorLogging {

    var maybeQuery: Option[String] = Some(initialQuery)

    val tick = context.system.scheduler.schedule(Duration.Zero, 5.seconds, self, UserActor.FetchTweets)

    def receive = {

        case UserActor.FetchTweets =>
            maybeQuery.foreach { query =>
                log.info(s"sending query: ${query}")
                tweetLoaderClient ! TweetLoader.LoadTweet(query)
            }
        case TweetLoader.NewTweet(tweet) => {
            log.info(s"pushing tweet lngth ${tweet.toString.length}")
            out(tweet)
        }

        case message: JsValue =>
            log.info(s"setting query: ${message}")
            maybeQuery = (message \ "query").asOpt[String]

    }

    override def postStop() {
        tick.cancel()
    }

}

object UserActor {

    def props(initialQuery:String, out:JsValue => Unit, tweetServiceClient: ActorRef): Props = {
        Props(new UserActor(initialQuery, out, tweetServiceClient))
    }

    case object FetchTweets



}