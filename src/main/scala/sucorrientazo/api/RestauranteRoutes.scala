package sucorrientazo.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives.{ pathPrefix, _ }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import sucorrientazo.actors.Entrega.EntregarListado
import sucorrientazo.actors.Service.ObtenerDirecciones
import sucorrientazo.util.CircuitBreaker._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait RestauranteRoutes extends Transformacion {
  implicit val actorSys: ActorSystem
  def actorService: ActorRef
  def entregaActor: ActorRef
  def materializer: ActorMaterializer
  lazy val log = Logging(actorSys, classOf[RestauranteRoutes])
  val logger = Logger(classOf[RestauranteRoutes])
  implicit lazy val timeout = Timeout(5.seconds)
  lazy val restauranteRoutes: Route = pathPrefix("entregar_almuerzos") {
    logger.info("ruta: http://localhost:8082/entregar_almuerzos/ ")
    logger.info("consumiendo el servicio en la ruta: http://localhost:8082/direcciones/ ")
    get {
      val response: Future[HttpResponse] =
        (actorService ? ObtenerDirecciones).mapTo[Future[HttpResponse]].flatten

      val mensaje: Future[String] = aplicarCircuitBreaker(response).map {
        httpResponse =>
          entregaActor ! EntregarListado(obtenerListadoParaEntrega(httpResponse))
          "Se ha iniciado el proceso, por favor consulte el log"
      }
      onSuccess(mensaje) { performed =>
        complete((StatusCodes.Created, "Esta es la respuesta ===============> " + performed))
      }
    }
  }
}
