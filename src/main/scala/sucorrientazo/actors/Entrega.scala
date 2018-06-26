package sucorrientazo.actors

import akka.actor.{ Actor, ActorLogging, Props }
import sucorrientazo.actors.Entrega.EntregarListado
import sucorrientazo.{ AlmuerzosMapper, Direcciones }

class Entrega extends Actor with ActorLogging {
  var i = 0

  override def receive: Receive = {
    case EntregarListado(almuerzosMapper) =>
      println("mensaje entregado al actor entrega")
      almuerzosMapper.almuerzos.map({
        almuerzos =>
          // creo cada uno de los actores
          val dron = context.actorOf(Dron.props, "dron" + i)
          val f = dron ! Direcciones(almuerzos.direcciones)
          sender() ! f
      })
  }
}

object Entrega {
  final case class EntregarListado(listadoMapper: AlmuerzosMapper)
  def props = Props[Entrega]
}
