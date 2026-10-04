package chat.client

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.Uri
import org.http4s.client.websocket.WSRequest
import org.http4s.jdkhttpclient.JdkWSClient

import chat.client.console.{Console, ConsoleEventRenderer}
import chat.client.transport.ChatClient
import chat.model.ChatEvent

object Main extends IOApp:
  private val errorMessage = "Usage: campfire-client <username>"
  private val greetingMessage = "Welcome to Campfire! Type a message and press Enter; /quit exits."
  private val serverUri = Uri.unsafeFromString("ws://127.0.0.1:8080")

  override def run(args: List[String]): IO[ExitCode] = IO
    .fromOption(retrieveUsername(args))(new IllegalArgumentException(errorMessage)).flatMap(connect)
    .as(ExitCode.Success).handleErrorWith { error =>
      IO.println(s"Connection failed: ${error.getMessage}").as(ExitCode.Error)
    }

  private def connect(username: String): IO[Unit] = (for {
    console <- Console.resource
    wsClient <- JdkWSClient.simple[IO]
    connection <- wsClient.connect(WSRequest(serverUri / "ws" / username))
  } yield (console, connection)).use { case (console, connection) =>
    val renderer = ConsoleEventRenderer(username)
    val onEvent = (event: ChatEvent) => renderer.render(event).fold(IO.unit)(console.printLine)

    for {
      _ <- console.printLine(greetingMessage)
      _ <- ChatClient(connection, onEvent, console.printLine).run(console.commands)
        .guarantee(console.printLine("Disconnected."))
    } yield ()
  }

  private def retrieveUsername(args: List[String]): Option[String] = args match
    case username :: Nil if username.trim.nonEmpty => Some(username.trim)
    case _                                         => None
