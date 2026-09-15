package chat

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpRoutes
import org.http4s.Request

object Main extends IOApp.Simple {
  private val routes = HttpRoutes.of[IO] {
    case GET -> Root / "health" =>
      Ok("ok")
  }
  override def run: IO[Unit] = {
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"127.0.0.1")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build
      .useForever
  }
}
