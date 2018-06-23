package sucorrientazo.actors

import akka.actor.{ Actor, ActorLogging, Props }
import sucorrientazo._
import sucorrientazo.configuration.Application._

class Dron extends Actor with ActorLogging {

  // TODO uso recursividad pata evitar crear var?
  var numeroAlmuerzosEntregados: Int = 0
  var coordenadasActuales: Coordenadas = Coordenadas(0, 0, Norte)

  override def receive: Receive = {
    /*
    * Un dron recibe una lista de direcciones de almuerzos a entregar.
    * debe haber un actor que maneje a todos los drones, que reciba
    * una lista de drones o en nuestro caso un AlmuerzosMapper, lo recorra
    * y vaya creando los actores que requiera y les vaya dando las direcciones.
     */
    case Direcciones(_) =>
      entregarAlmuerzos(_)
  }

  def entregarAlmuerzos(direcciones: List[Direcciones]): Reporte = {
    val entrega: List[String] = direcciones.flatMap {
      _.direcciones.map {
        almuerzo =>
          val coordenadas = entregarUnAlmuerzo(almuerzo.movimientos)
          incrementar()
          s"(${coordenadas.x}, ${coordenadas.y}) ${coordenadas.posicion}"
      }
    }
    // almacenar este reporte en base de datos
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
