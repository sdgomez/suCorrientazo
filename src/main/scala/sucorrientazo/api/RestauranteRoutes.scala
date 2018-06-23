package sucorrientazo.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.{ HttpEntity, HttpResponse }
import akka.http.scaladsl.server.Directives.{ pathPrefix, _ }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.pattern.{ CircuitBreaker, ask }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.{ ByteString, Timeout }
import play.api.libs.json.{ JsObject, JsValue, Json }
import sucorrientazo.{ Almuerzos, AlmuerzosMapper }
import sucorrientazo.actors.Service.ObtenerDirecciones

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

trait RestauranteRoutes extends RestauranteMarshaller {
  implicit val actorSys: ActorSystem
  def actorService: ActorRef
  def materializer: ActorMaterializer
  lazy val log = Logging(actorSys, classOf[RestauranteRoutes])
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

      /*(1 to 100).map(x => {
        Thread.sleep(50)
        val random = Random.nextInt(3)
        println("random = " + random)
        val fallaAveces: Future[HttpResponse] = if (random == 2) {
          response
          // convertir este json a una lista de direcciones de almuerzos
          // llamar al actor que reparte los almuerzos
          // almacenar en base de datos los almuerzos entregados
        } else {
          Future.failed(new Exception("Falla aveces"))
        } */

      val askFuture: Future[HttpResponse] = breaker.withCircuitBreaker(response)

      val fAlmuerzos: Future[AlmuerzosMapper] = askFuture.map {
        httpResponse =>
          // println("este es el JSON" + httpResponse.entity.toString)
          val entity = httpResponse.entity.asInstanceOf[HttpEntity.Strict]
          val entidad = entity.data.utf8String
          val json: JsObject = Json.parse(entidad).as[JsObject]
          println("este es mi json en string ====================> " + entity.data.utf8String)

          //json.as[Almuerzos]
          transformarJson(json)
      }

      fAlmuerzos.map(x => println(s"ha tenido exito $x.entity con el random /* random */"))
        .recover({
          case t => "error: " + println(t.toString)
        })
      //})

      // Thread.sleep(2000000)
      complete("ok")

    }
  }

}
