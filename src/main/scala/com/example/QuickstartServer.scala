package com.example

//#quick-start-server
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import sucorrientazo.actors.Service
import sucorrientazo.api.RestauranteRoutes

import scala.concurrent.Await
import scala.concurrent.duration.Duration

//#main-class
object QuickstartServer extends App with RestauranteRoutes {

  implicit val actorSys: ActorSystem = ActorSystem("almuerzos")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val actorService: ActorRef = actorSys.actorOf(Service.props, "userRegistryActor")

  lazy val routes: Route = restauranteRoutes
  //#main-class

  //#http-server
  Http().bindAndHandle(routes, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(actorSys.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
