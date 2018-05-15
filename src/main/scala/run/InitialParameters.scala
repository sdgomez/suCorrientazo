package run

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait InitialParameters {
  implicit val actorSys: ActorSystem = ActorSystem("almuerzos")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
