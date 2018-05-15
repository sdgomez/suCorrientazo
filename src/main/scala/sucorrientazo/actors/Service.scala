package sucorrientazo.actors

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import sucorrientazo.actors.Service.ObtenerDirecciones

import scala.concurrent.Future

class Service extends Actor with ActorLogging {
  implicit val actorSys: ActorSystem = context.system
  override def receive: Receive = {
    case ObtenerDirecciones =>
      val drones: Future[HttpResponse] =
        Http().singleRequest(HttpRequest(uri = "http://localhost:8082/direcciones/"))
      sender() ! drones
  }
}

object Service {
  final case object ObtenerDirecciones
  def props: Props = Props[Service]
}
