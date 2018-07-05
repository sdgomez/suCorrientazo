package sucorrientazo.api

import akka.http.scaladsl.model.{ HttpEntity, HttpResponse }
import play.api.libs.json._
import sucorrientazo._

trait Transformacion {

  def obtenerListadoParaEntrega(response: HttpResponse): AlmuerzosMapper = {
    val entity = response.entity.asInstanceOf[HttpEntity.Strict] // TODO hacer la conversion de una mejor forma
    transformarJson(Json.parse(entity.data.utf8String).as[JsObject])
  }

  def transformarJson(jsValue: JsObject): AlmuerzosMapper = {

    val jsArray: JsArray = (jsValue \ "almuerzos").as[JsArray]
    val direcciones = jsArray.value.toList.map {
      x =>
        (x.as[JsObject] \ "direcciones").as[JsArray].value.toList.map(_.toString)
    }
    val numeroDrones: Int = (jsValue \ "numero_drones").as[Int]
    AlmuerzosMapper(mapToDirecciones(Almuerzos(direcciones, numeroDrones)).almuerzos, numeroDrones)
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
