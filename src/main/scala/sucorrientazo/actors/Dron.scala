package sucorrientazo.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.typesafe.scalalogging.Logger
import sucorrientazo._
import sucorrientazo.configuration.Application._

import scala.util.Random

class Dron extends Actor with ActorLogging {

  val logger = Logger(classOf[Dron])
  /*override def preStart(): Unit = logger.debug(s"${this.self.path.name} started")
  override def postStop(): Unit = logger.debug(s"${this.self.path.name} stopped")*/

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
      irPosicionInicial
      fallar()
      entregarAlmuerzos(x)
  }

  private def fallar() =
    if (Random.nextInt(3) == 2) {
      //new Exception(s"Actor ${this.self.path.name} falla.")
      throw new NullPointerException
    }

  def entregarAlmuerzos(direcciones: List[AlmuerzoMapper]) = {
    val entrega: List[String] = direcciones.map {
      x =>
        val coordenadas = entregarUnAlmuerzo(x.movimientos)
        incrementar()
        s"(ACTOR_DRON = ${this.self.path.name}, ${coordenadas.x}, ${coordenadas.y}, ${coordenadas.posicion})"
    }
    logger.info(s"**************************************** " +
      s"REPORTE = ${Reporte(entrega)} *******************************************")
  }

  def entregarUnAlmuerzo(direccion: List[Movimiento]): Coordenadas = {
    val nuevaCoordenada: Coordenadas = direccion.map {
      case Avanzar =>
        avanzar(coordenadasActuales.posicion)
      case GirarDerecha =>
        girarAlaDerecha(coordenadasActuales.posicion)
      case _ =>
        girarIzquierda(coordenadasActuales.posicion)
    }.last
    resetearValores()
    nuevaCoordenada
  }

  def avanzar(posicion: Posicion): Coordenadas = {
    posicion match {
      case Norte => actualizarCoordenadas(
        Coordenadas(coordenadasActuales.x, coordenadasActuales.y + 1, coordenadasActuales.posicion)
      )
      case Sur => actualizarCoordenadas(
        Coordenadas(coordenadasActuales.x, coordenadasActuales.y - 1, coordenadasActuales.posicion)
      )
      case Oeste => actualizarCoordenadas(
        Coordenadas(coordenadasActuales.x - 1, coordenadasActuales.y, coordenadasActuales.posicion)
      )
      case _ => actualizarCoordenadas(
        Coordenadas(coordenadasActuales.x + 1, coordenadasActuales.y, coordenadasActuales.posicion)
      )
    }
  }

  def girarAlaDerecha(posicion: Posicion): Coordenadas = {
    posicion match {
      case Norte =>
        actualizarCoordenadas(
          Coordenadas(coordenadasActuales.x, coordenadasActuales.y, Este)
        )
      case Sur => actualizarCoordenadas(
        Coordenadas(coordenadasActuales.x, coordenadasActuales.y, Oeste)
      )
      case Oeste => actualizarCoordenadas(
        Coordenadas(coordenadasActuales.x, coordenadasActuales.y, Norte)
      )
      case _ =>
        actualizarCoordenadas(
          Coordenadas(coordenadasActuales.x, coordenadasActuales.y, Sur)
        )
    }
  }

  def girarIzquierda(posicion: Posicion): Coordenadas = {
    posicion match {
      case Norte =>
        actualizarCoordenadas(Coordenadas(coordenadasActuales.x, coordenadasActuales.y, Oeste))
      case Sur =>
        actualizarCoordenadas(Coordenadas(coordenadasActuales.x, coordenadasActuales.y, Este))
      case Oeste =>
        actualizarCoordenadas(Coordenadas(coordenadasActuales.x, coordenadasActuales.y, Sur))
      case _ =>
        actualizarCoordenadas(Coordenadas(coordenadasActuales.x, coordenadasActuales.y, Norte))
    }
  }

  def incrementar(): Unit = {
    numeroAlmuerzosEntregados = numeroAlmuerzosEntregados + 1
  }

  def actualizarCoordenadas(coordenadas: Coordenadas): Coordenadas = {
    coordenadasActuales = coordenadas
    coordenadasActuales
  }

  def resetearValores(): Unit = {
    if (numeroAlmuerzosEntregados == maxNumeroEntregasPorDron) {
      coordenadasActuales = Coordenadas(0, 0, Norte)
      numeroAlmuerzosEntregados = 0
    }
  }
  def irPosicionInicial(): Unit ={
    coordenadasActuales = Coordenadas(0, 0, Norte)
    numeroAlmuerzosEntregados = 0
  }
}

object Dron {
  def props: Props = Props[Dron]
}
