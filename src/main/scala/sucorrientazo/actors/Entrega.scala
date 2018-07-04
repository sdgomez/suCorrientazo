package sucorrientazo.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy }
import akka.pattern.{ Backoff, BackoffSupervisor }
import com.typesafe.scalalogging.Logger
import sucorrientazo.{ AlmuerzosMapper, Direcciones }

import scala.concurrent.duration._
import scala.util.Random

class Entrega extends Actor with ActorLogging {
  import Entrega._
  val logger = Logger(classOf[Entrega])

  var i = 0

  override def receive: Receive = {
    case EntregarListado(almuerzosMapper) =>
      logger.debug("mensaje entregado al actor entrega")
      val dronesDisponibles: List[ActorRef] = crearActores(almuerzosMapper.numero_drones)
      val posicionDron: Int = Random.nextInt(dronesDisponibles.length)

      almuerzosMapper.almuerzos.map {
        almuerzos =>
          //val dron: ActorRef = supervise(Dron.props, s"dron-${i}", s"supervisor-${i}")
          val dron: ActorRef = dronesDisponibles(posicionDron)
          dron ! Direcciones(almuerzos.direcciones)
        // i = i + 1
      }
  }

  def crearActores(numeroActores: Int): List[ActorRef] = {
    val x = for { a <- 1 to numeroActores } yield {
      supervise(Dron.props, s"dron-${a}", s"supervisor-${a}")
    }
    x.toList
  }

  def supervise(childProps: Props, name: String, supervisorName: String): ActorRef = {
    val supervisor = BackoffSupervisor.props(
      Backoff.onStop(
        childProps,
        childName = name,
        minBackoff = 1.seconds,
        maxBackoff = 3.seconds,
        randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
      ).withSupervisorStrategy(
        OneForOneStrategy() {
          case ex: Exception =>
            log.error(ex, s"el actor  ${name.toUpperCase} esta siendo atendido")
            SupervisorStrategy.restart
          //context.child(name) ! Start
          case _ => SupervisorStrategy.escalate
        }
      )
    )

    context.actorOf(supervisor, supervisorName)
  }

}

object Entrega {
  final case class EntregarListado(listadoMapper: AlmuerzosMapper)
  def props = Props[Entrega]
}
