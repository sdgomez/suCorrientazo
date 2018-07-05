package sucorrientazo.configuration

import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.duration.{ Duration, FiniteDuration, SECONDS }

object Application {
  val config: Config = ConfigFactory.load()
  val app: Config = config.getConfig("app")
  val dron: Config = app.getConfig("dron")
  val maxNumeroEntregasPorDron: Int = dron.getInt("maximoNumeroEntregas")

  //Parametros para el Circuit Breaker
  val service: Config = app.getConfig("service")
  val maxFailures: Int = service.getInt("maxFailures")
  val callTimeout: FiniteDuration = FiniteDuration(service.getLong("callTimeout"), SECONDS)
  val resetTimeout: FiniteDuration = FiniteDuration(service.getLong("resetTimeout"), SECONDS)

  //parametros para la supervision
  val backoff: Config = app.getConfig("backoff")
  val minBackoff: FiniteDuration = FiniteDuration(backoff.getLong("minBackoff"), SECONDS)
  val maxBackoff: FiniteDuration = FiniteDuration(backoff.getLong("maxBackoff"), SECONDS)
  val randomFactor: Double = backoff.getDouble("randomFactor")
}