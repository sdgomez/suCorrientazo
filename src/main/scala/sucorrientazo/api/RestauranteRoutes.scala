package sucorrientazo.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives.{ pathPrefix, _ }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.pattern.{ CircuitBreaker, ask }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import play.api.libs.json.{ JsObject, Json }
import sucorrientazo.AlmuerzosMapper
import sucorrientazo.actors.Entrega.EntregarListado
import sucorrientazo.actors.Service.ObtenerDirecciones

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait RestauranteRoutes extends RestauranteMarshaller {
  implicit val actorSys: ActorSystem
  def actorService: ActorRef
  def entregaActor: ActorRef
  def materializer: ActorMaterializer
  lazy val log = Logging(actorSys, classOf[RestauranteRoutes])
  val logger = Logger(classOf[RestauranteRoutes])
  implicit lazy val timeout = Timeout(5.seconds)
  // http://localhost:8080/entregar_almuerzos
  lazy val restauranteRoutes: Route = pathPrefix("entregar_almuerzos") {
    // http://localhost:8082/direcciones/
    get {
      val response: Future[HttpResponse] =
        (actorService ? ObtenerDirecciones).mapTo[Future[HttpResponse]].flatten

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

      val askFuture: Future[HttpResponse] = breaker.withCircuitBreaker(response)

      val fAlmuerzos: Future[String] = askFuture.map {
        httpResponse =>
          val entity = httpResponse.entity.asInstanceOf[HttpEntity.Strict]
          val entidad = entity.data.utf8String
          val json: JsObject = Json.parse(entidad).as[JsObject]
          val almuerzoMapper: AlmuerzosMapper = transformarJson(json)
          entregaActor ! EntregarListado(almuerzoMapper)
          "Se ha iniciado el proceso, por favor consulte el log"
      }

      onSuccess(fAlmuerzos) { performed =>
        complete((StatusCodes.Created, "Esta es la respuesta ===============> " + performed))
      }
    }
  }

}
