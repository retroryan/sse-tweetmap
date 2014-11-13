package backend

import akka.actor._
import actors.TweetLoader
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory
import scala.io.StdIn

/**
 * Main class for starting shard nodes.
 */
object MainTweetLoader  {

    def main(args: Array[String]): Unit = {
        //The first parameter is the port to start the remote listener on.  It defaults to 0
        //which causes akka to start the remote listener on a random port.
        val system = if (args.isEmpty)
            startSystem("0")
        else
            startSystem(args(0))

        initialize(system)
        commandLoop(system)
    }

    def startSystem(port: String) = {

        val role = "backend-loader"
        val config = ConfigFactory.parseString(s"akka.cluster.roles=[$role]").
            withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port)).
            withFallback(ConfigFactory.load())

        // Create an actor system with the name of application - this is the same name
        // that play uses for it's actor system.  The names need to be the same so they
        // can join together in a cluster.
        ActorSystem("application", config)
    }

    def commandLoop(system: ActorSystem): Unit = {
        val line: String = StdIn.readLine()
        if (line.startsWith("s")) {
            system.shutdown()
        } else {
            commandLoop(system)
        }
    }

    def initialize(system: ActorSystem): Unit = {
        //verify that this cluster node is running with the role of "backend-loader"
        if (Cluster(system).selfRoles.exists(r => r.startsWith("backend-loader"))) {
            system.actorOf(TweetLoader.props(), "tweetLoader")
        }
    }


}
