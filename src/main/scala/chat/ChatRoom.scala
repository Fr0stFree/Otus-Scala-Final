package chat

import cats.effect.IO
import fs2.Stream
import fs2.concurrent.Topic
import org.http4s.websocket.WebSocketFrame

final class ChatRoom private (topic: Topic[IO, String]):
  def publish(message: String): IO[Unit] =
    val normalized = message.trim
    if normalized.nonEmpty then topic.publish1(normalized).void else IO.unit

  def messages: Stream[IO, String] =
    topic.subscribe(1000)

  def frames: Stream[IO, WebSocketFrame] =
    messages.map(WebSocketFrame.Text(_))

object ChatRoom:
  def create: IO[ChatRoom] =
    Topic[IO, String].map(new ChatRoom(_))
