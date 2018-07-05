package sucorrientazo.util

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import sucorrientazo.configuration.Application.{ callTimeout, maxFailures, resetTimeout }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CircuitBreaker {
  def aplicarCircuitBreaker[T](future: Future[T])(implicit actorSys: ActorSystem): Future[T] = {
    val breaker: CircuitBreaker =
      new CircuitBreaker(
        actorSys.scheduler,
        maxFailures = maxFailures,
        callTimeout = callTimeout,
        resetTimeout = resetTimeout
      ).
        onOpen(println("circuit breaker opened!")).
        onClose(println("circuit breaker closed!")).
        onHalfOpen(println("circuit breaker half-open"))
    breaker.withCircuitBreaker(future)
  }
}
