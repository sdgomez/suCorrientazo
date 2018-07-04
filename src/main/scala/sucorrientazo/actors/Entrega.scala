package sucorrientazo.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSelection, OneForOneStrategy, Props, SupervisorStrategy }
import akka.pattern.{ Backoff, BackoffSupervisor }
import com.typesafe.scalalogging.Logger
import sucorrientazo.{ AlmuerzosMapper, Direcciones }

import scala.concurrent.duration._
import scala.util.Random

class Entrega extends Actor with ActorLogging {
  import Entrega._
  val logger = Logger(classOf[Entrega])

  override def receive: Receive = {
    case EntregarListado(almuerzosMapper) =>
      logger.debug("mensaje entregado al actor entrega")
      val dronesDisponibles: List[ActorSelection] = crearActores(almuerzosMapper.numero_drones)
      almuerzosMapper.almuerzos.map {
        almuerzos =>
          //val dron: ActorRef = supervise(Dron.props, s"dron-${i}", s"supervisor-${i}")
          val posicionDron: Int = Random.nextInt(dronesDisponibles.length)
          val dron: ActorSelection = dronesDisponibles(posicionDron)
          dron ! Direcciones(almuerzos.direcciones)
      }
  }

  def crearActores(numeroActores: Int): List[ActorSelection] = {
    (for { a <- 1 to numeroActores } yield {
      supervise(Dron.props, s"dron-${a}", s"supervisor-${a}")
    }).toList
  }

  def supervise(childProps: Props, name: String, supervisorName: String): ActorSelection = {
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
    val actorSupervisor: Option[ActorRef] = context.child(supervisorName)
    if (actorSupervisor.isDefined) {
      context.actorSelection(s"akka://almuerzos/user/entregaActor/${supervisorName}")
    } else {
      val nuevoActorSupervisor: ActorRef = context.actorOf(supervisor, supervisorName)
      context.actorSelection(nuevoActorSupervisor.path)
    }
  }

}

object Entrega {
  final case class EntregarListado(listadoMapper: AlmuerzosMapper)
  def props = Props[Entrega]
}
