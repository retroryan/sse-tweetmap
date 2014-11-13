package controllers

import actors.{Actors, UserActor}
import akka.actor.Props
import play.api.Play.current
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{WebSocket, Action, Controller}

import scala.concurrent.Future
import play.api.libs.concurrent.Akka
import play.api.libs.EventSource
import play.api.libs.iteratee.{Iteratee, Concurrent}

object Application extends Controller {

    def index = Action {
        Ok(views.html.index("TweetMap"))
    }

    def search(query: String) = Action.async {
        Future.successful(Ok(Json.toJson("Not Implemented anymore")))
    }

    /**

    def ws = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
        UserActor.props(out)
    }

  **/


    /** Serves Server Sent Events over HTTP connection */
    def tweetFeed(query: String) = Action {
        implicit req => {
            /** Creates enumerator and channel for Strings through Concurrent factory object
              * for pushing data through the WebSocket */
            val (out, wsOutChannel) = Concurrent.broadcast[JsValue]

            val ref = Akka.system.actorOf(UserActor.props(query, jsValue => wsOutChannel.push(jsValue), Actors.tweetLoaderDirect))

            Ok.feed(out
                &> EventSource()).as("text/event-stream")
        }
    }
}
