package sucorrientazo.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.typesafe.scalalogging.Logger
import sucorrientazo._
import sucorrientazo.configuration.Application._

class Dron extends Actor with ActorLogging {

  val logger = Logger(classOf[Dron])

  // TODO uso recursividad pata evitar crear var?
  var numeroAlmuerzosEntregados: Int = 0
  var coordenadasActuales: Coordenadas = Coordenadas(0, 0, Norte)

  override def receive: Receive = {
    case Direcciones(x) =>
      // logger.info("mensaje recibido" + this.self)
      sender() ! entregarAlmuerzos(x)
  }

  def entregarAlmuerzos(direcciones: List[AlmuerzoMapper]): Reporte = {
    val entrega: List[String] = direcciones.map {
      x =>
        val coordenadas = entregarUnAlmuerzo(x.movimientos)
        incrementar()
        // logger.info(s" =================================== ALMUERZO ENTREGADO POR EL ACTOR DRON ${this.self} EN LAS SIGUIENTES COORDENADAS =  (${coordenadas.x}, ${coordenadas.y}, ${coordenadas.posicion}) ===================================")
        s"(ACTOR_DRON = ${this.self.path.name}, ${coordenadas.x}, ${coordenadas.y}, ${coordenadas.posicion})"
    }
    // almacenar este reporte en base de datos

    logger.info(s"**************************************** REPORTE = ${Reporte(entrega)} *******************************************")
    logger.info("*******************************************************************************************************************")
    logger.info(" ")
    Reporte(entrega)
  }

  def entregarUnAlmuerzo(direccion: List[Movimiento]): Coordenadas = {
    val nuevaCoordenada: Coordenadas = direccion.map {
      case Avanzar =>
        avanzar(coordenadasActuales.posicion)
      case GirarDerecha =>
        girarAlaDerecha(coordenadasActuales.posicion)
      case _ =>
        girarDerecha(coordenadasActuales.posicion)
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

  def girarDerecha(posicion: Posicion): Coordenadas = {
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
}

object Dron {
  def props: Props = Props[Dron]
}
