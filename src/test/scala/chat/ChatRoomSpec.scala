package chat

import cats.effect.IO
import fs2.Stream
import munit.CatsEffectSuite

import scala.concurrent.duration.*

class ChatRoomSpec extends CatsEffectSuite:
  test("publish sends message to subscribers"):
    for
      room <- ChatRoom.create
      received <- room.messages
        .take(1)
        .concurrently(Stream.awakeEvery[IO](50.millis).evalMap(_ => room.publish("hello")))
        .compile
        .lastOrError
        .timeout(2.seconds)
    yield assertEquals(received, "hello")

  test("publish ignores blank messages"):
    for
      room <- ChatRoom.create
      received <- room.messages
        .take(1)
        .concurrently(fs2.Stream.eval(room.publish("   ")))
        .compile
        .last
        .timeoutTo(200.millis, IO.pure(None))
    yield assertEquals(received, None)
