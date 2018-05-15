package sucorrientazo.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives.{ pathPrefix, _ }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.pattern.CircuitBreaker
import akka.util.Timeout
import sucorrientazo.actors.Service
import sucorrientazo.actors.Service.ObtenerDirecciones

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random
import akka.pattern.ask

trait RestauranteRoutes {
  implicit val actorSys: ActorSystem
  // def actorService: ActorRef
  lazy val log = Logging(actorSys, classOf[RestauranteRoutes])
  implicit lazy val timeout = Timeout(5.seconds)
  // http://localhost:8080/entregar_almuerzos
  lazy val restauranteRoutes: Route = pathPrefix("entregar_almuerzos") {
    // http://localhost:8082/direcciones/
    get {
      val actorService: ActorRef = actorSys.actorOf(Service.props, "userRegistryActor")
      // (userRegistryActor ? CreateUser(user)).mapTo[ActionPerformed]
      val drones: Future[Future[HttpResponse]] = (actorService ? ObtenerDirecciones).mapTo[Future[HttpResponse]]
      val d2: Future[HttpResponse] = drones.flatten
      // Http().singleRequest(HttpRequest(uri = "http://localhost:8082/direcciones/"))

      val breaker =
        new CircuitBreaker(
          actorSys.scheduler,
          maxFailures = 1,
          callTimeout = 1 seconds,
          resetTimeout = 1 seconds
        ).
          onOpen(println("circuit breaker opened!")).
          onClose(println("circuit breaker closed!")).
          onHalfOpen(println("circuit breaker half-open"))

      (1 to 100).map(x => {
        Thread.sleep(50)
        val random = Random.nextInt(3)
        println("random = " + random)
        val fallaAveces: Future[HttpResponse] = if (random == 2) {
          d2
          // convertir este json a una lista de direcciones de almuerzos
          // llamar al actor que reparte los almuerzos
          // almacenar en base de datos los almuerzos entregados
        } else {
          Future.failed(new Exception("Falla aveces"))
        }

        val askFuture: Future[HttpResponse] = breaker.withCircuitBreaker(fallaAveces)

        /*onSuccess(askFuture) { extraction =>
          complete(extraction)
        }*/

        askFuture.map(x => println(s"ha tenido exito $x.entity con el random $random")).recover({
          case t => "error: " + println(t.toString)
        })
      })

      Thread.sleep(2000000)
      complete("ok")

    }
  }

}
