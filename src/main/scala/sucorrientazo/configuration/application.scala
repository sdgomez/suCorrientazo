package sucorrientazo.configuration

import com.typesafe.config.{ Config, ConfigFactory }

object Application {
  val config: Config = ConfigFactory.load()
  val app: Config = config.getConfig("app")
  val dron: Config = app.getConfig("dron")
  val maxNumeroEntregasPorDron: Int = dron.getInt("maximoNumeroEntregas")
}
