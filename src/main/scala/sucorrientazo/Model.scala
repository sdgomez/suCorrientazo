package sucorrientazo

trait Model
trait Posicion
trait Movimiento
final case object Norte extends Posicion
final case object Sur extends Posicion
final case object Este extends Posicion
final case object Oeste extends Posicion
final case class Coordenadas(x: Int, y: Int, posicion: Posicion) extends Model

final case class Almuerzo(direcciones: List[String], drones: Int)
final case class Almuerzos(almuerzos: List[List[String]], numero_drones: Int)

final case class AlmuerzoMapper(movimientos: List[Movimiento]) extends Model
final case class Direcciones(direcciones: List[AlmuerzoMapper])
final case class AlmuerzosMapper(
  almuerzos: List[Direcciones],
  numero_drones: Int
) extends Model

final case class Reporte(resultados: List[String])
final case class Fichero(nombre: String) extends Model
final case object Avanzar extends Movimiento
final case object GirarIzquierda extends Movimiento
final case object GirarDerecha extends Movimiento

final case class EntradasDron(direcciones: List[String])
final case class EntradasDrones(drones: List[EntradasDron])

final case object ObtenerDirecciones
