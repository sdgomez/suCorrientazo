package sucorrientazo.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.typesafe.scalalogging.Logger
import sucorrientazo.{ AlmuerzosMapper, Direcciones }

class Entrega extends Actor with ActorLogging {
  import Entrega._
  val logger = Logger(classOf[Entrega])

  var i = 0

  override def receive: Receive = {
    case EntregarListado(almuerzosMapper) =>
      logger.debug("mensaje entregado al actor entrega")
      almuerzosMapper.almuerzos.map({
        almuerzos =>
          // creo cada uno de los actores
          val dron = context.actorOf(Dron.props, "dron" + i)
          val f = dron ! Direcciones(almuerzos.direcciones)
          i = i + 1
          sender() ! f
      })

    case "prueba" =>
      println("hola")
      sender ! "Hola"
  }
}

object Entrega {
  final case class EntregarListado(listadoMapper: AlmuerzosMapper)
  def props = Props[Entrega]
}
