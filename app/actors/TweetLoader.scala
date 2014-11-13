package actors

import akka.actor._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import scala.util._
import scala.util.Failure
import scala.util.Success
import play.api.libs.json.JsObject
import utils.WSUtils
import play.api.libs.ws.WSResponse

/**
 * Tweet Loader Actor
 */
class TweetLoader extends Actor with ActorLogging with SettingsActor {

    override def receive: Receive = {

        case TweetLoader.LoadTweet(search) => {
            val querySender = sender()

            implicit val ec: ExecutionContext = context.system.dispatcher

            fetchTweets(search) onComplete {
                case Success(respJson) ⇒ {
                    querySender ! TweetLoader.NewTweet(respJson)
                }
                case Failure(f) ⇒ {
                    log.info(s"tweet loader failed!")
                    sender() ! Status.Failure(f)
                }
            }
        }

    }

    // searches for tweets based on a query
    def fetchTweets(query: String)(implicit ec: ExecutionContext): Future[JsValue] = {

        val tweetsFuture =  WSUtils.url(settings.TWEET_SEARCH_URL).withQueryString("q" -> query).get()

        tweetsFuture.flatMap { response =>
            tweetLatLon((response.json \ "statuses").as[Seq[JsValue]])
        } recover {
            case errMsg => {
                log.error(s"ERROR Loading Tweets: $errMsg")
                Seq.empty[JsValue]
            }
        } map { tweets =>
            Json.obj("statuses" -> tweets)
        }

    }

    private def putLatLonInTweet(latLon: JsValue) = __.json.update(__.read[JsObject].map(_ + ("coordinates" -> Json.obj("coordinates" -> latLon))))

    private def tweetLatLon(tweets: Seq[JsValue])(implicit ec: ExecutionContext): Future[Seq[JsValue]] = {
        val tweetsWithLatLonFutures = tweets.map { tweet =>

            if ((tweet \ "coordinates" \ "coordinates").asOpt[Seq[Double]].isDefined) {
                Future.successful(tweet)
            } else {
                val latLonFuture: Future[(Double, Double)] = (tweet \ "user" \ "location").asOpt[String]
                    .map(lookupLatLon)
                    .getOrElse(Future.successful(randomLatLon))


                latLonFuture.map { latLon =>
                    tweet.transform(putLatLonInTweet(Json.arr(latLon._2, latLon._1))).getOrElse(tweet)
                }
            }
        }

        Future.sequence(tweetsWithLatLonFutures)
    }

    private def randomLatLon: (Double, Double) = ((Random.nextDouble * 180) - 90, (Random.nextDouble * 360) - 180)

    private def lookupLatLon(query: String)(implicit ec: ExecutionContext): Future[(Double, Double)] = {
        val locationFuture =  WSUtils.url(settings.GEOCODE_URL).withQueryString(
            "sensor" -> "false",
            "address" -> query)
            .get()

        locationFuture.map { response =>
            (response.json \\ "location").headOption.map { location =>
                ((location \ "lat").as[Double], (location \ "lng").as[Double])
            }.getOrElse(randomLatLon)
        }
    }
}

object TweetLoader {

    case class LoadTweet(search: String)

    case class NewTweet(tweet: JsValue)

    def props(): Props = {
        Props(new TweetLoader())
    }
}
