package sucorrientazo.api

import play.api.libs.json._
import sucorrientazo._

trait RestauranteMarshaller {

  def transformarJson(jsValue: JsObject): AlmuerzosMapper = {

    val jsArray: JsArray = (jsValue \ "almuerzos").as[JsArray]
    val direcciones = jsArray.value.toList.map {
      x =>
        val m: JsObject = x.as[JsObject]
        val o = (m \ "direcciones").as[JsArray].value.toList.map(_.toString)
        o
    }
    val numeroDrones: Int = (jsValue \ "numero_drones").as[Int]
    val z = mapToDirecciones(Almuerzos(direcciones, numeroDrones))
    println("Posiciones Mapeadas " + z)
    AlmuerzosMapper(z.almuerzos, numeroDrones)
  }

  def mapToDirecciones(almuerzos: Almuerzos): AlmuerzosMapper = {
    AlmuerzosMapper(
      almuerzos.almuerzos.map {
        m =>
          Direcciones(
            m.map {
              direccion =>
                AlmuerzoMapper(direccion.toList.map(stringToPosicion))
            }
          )
      }, 0
    )
  }

  def stringToPosicion(caracter: Char): Movimiento = {
    caracter match {
      case 'A' => Avanzar
      case 'D' => GirarDerecha
      case _ => GirarIzquierda
    }
  }

}
