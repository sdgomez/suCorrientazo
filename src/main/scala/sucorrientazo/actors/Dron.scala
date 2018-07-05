package sucorrientazo.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.typesafe.scalalogging.Logger
import sucorrientazo._
import sucorrientazo.configuration.Application._

import scala.util.Random

class Dron extends Actor with ActorLogging {

  val logger = Logger(classOf[Dron])

  override def preStart(): Unit = println(s"supervised actor ${this.self.path.name} started")
  override def postStop(): Unit = println(s"supervised actor ${this.self.path.name} stopped")
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = println(s"Se va reiniciar el actor ${this.self.path.name}")
  override def postRestart(reason: Throwable): Unit = println(s"El actor supervisado ha sido reiniciado ${this.self.path.name}")

  override def aroundPostRestart(reason: Throwable): Unit = logger.debug(s"${this.self.path.name} re-started")

  // TODO uso recursividad pata evitar crear var?
  var numeroAlmuerzosEntregados: Int = 0
  var coordenadasActuales: Coordenadas = Coordenadas(0, 0, Norte)

  override def receive: Receive = {
    case Direcciones(x) =>
      entregarAlmuerzos(x)
  }

  def entregarAlmuerzos(direcciones: List[AlmuerzoMapper]) = {
    val entrega: List[String] = direcciones.map {
      x =>
        entregarUnAlmuerzo(x.movimientos)
        incrementar
        s"(ACTOR_DRON = ${this.self.path.name}, ${coordenadasActuales.x}, ${coordenadasActuales.y}, ${coordenadasActuales.posicion})"
    }
    logger.info(s"**************************************** " +
      s"REPORTE = ${Reporte(entrega)} *******************************************")
  }

  def entregarUnAlmuerzo(direccion: List[Movimiento]): Unit = {
    direccion.map {
      case Avanzar =>
        avanzar(coordenadasActuales.posicion)
      case GirarDerecha =>
        girarAlaDerecha(coordenadasActuales.posicion)
      case _ =>
        girarDerecha(coordenadasActuales.posicion)
    }.last
    resetearValores()
  }

  def avanzar(posicion: Posicion): Coordenadas = {
    posicion match {
      case Norte =>
        coordenadasActuales.copy(y = coordenadasActuales.y + 1)
      case Sur =>
        coordenadasActuales.copy(y = coordenadasActuales.y - 1)
      case Oeste =>
        coordenadasActuales.copy(x = coordenadasActuales.x - 1)
      case _ =>
        coordenadasActuales.copy(x = coordenadasActuales.x + 1)
    }
  }

  def girarAlaDerecha(posicion: Posicion): Coordenadas = {
    posicion match {
      case Norte =>
        coordenadasActuales.copy(posicion = Este)
      case Sur =>
        coordenadasActuales.copy(posicion = Oeste)
      case Oeste =>
        coordenadasActuales.copy(posicion = Norte)
      case _ =>
        coordenadasActuales.copy(posicion = Sur)
    }
  }

  def girarDerecha(posicion: Posicion): Coordenadas = {
    posicion match {
      case Norte =>
        coordenadasActuales.copy(posicion = Oeste)
      case Sur =>
        coordenadasActuales.copy(posicion = Este)
      case Oeste =>
        coordenadasActuales.copy(posicion = Sur)
      case _ =>
        coordenadasActuales.copy(posicion = Norte)
    }
  }

  def incrementar(): Unit = {
    numeroAlmuerzosEntregados = numeroAlmuerzosEntregados + 1
  }

  def resetearValores(): Unit = {
    if (numeroAlmuerzosEntregados == maxNumeroEntregasPorDron) {
      coordenadasActuales = Coordenadas(0, 0, Norte)
      numeroAlmuerzosEntregados = 0
    }
  }
}

object Dron {
  def props: Props = Props[Dron]
}
