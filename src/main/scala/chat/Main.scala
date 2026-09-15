package chat

import cats.effect.IO
import cats.effect.IOApp
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder

object Main extends IOApp.Simple:
  val run: IO[Unit] =
    for
      room <- ChatRoom.create
      app = new ChatRoutes(room).routes.orNotFound
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(app)
        .build
        .use(_ => IO.never)
    yield ()
