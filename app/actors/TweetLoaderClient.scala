package actors

import akka.actor.{ActorLogging, Actor, Props}
import akka.routing.FromConfig

object TweetLoaderClient {
    def props(): Props = Props(new TweetLoaderClient())
}

/**
 * A client for the tweet loader, handles routing of the fetch tweet messages to the actual tweet loader
 */
class TweetLoaderClient extends Actor with ActorLogging {

    val tweetLoaderRouter = context.actorOf(Props.empty.withRouter(FromConfig), "router")

    def receive = {
        case TweetLoader.LoadTweet(search) => {
            log.info(s"forwarding search for $search to router")
            tweetLoaderRouter forward TweetLoader.LoadTweet(search)
        }
    }
}