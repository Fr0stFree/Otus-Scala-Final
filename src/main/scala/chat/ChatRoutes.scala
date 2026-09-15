package chat

import cats.effect.IO
import fs2.Pipe
import org.http4s.HttpRoutes
import org.http4s.MediaType
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

final class ChatRoutes(chatRoom: ChatRoom):
  private def receive(user: String): Pipe[IO, WebSocketFrame, Unit] =
    _.collect { case WebSocketFrame.Text(text, _) => s"[$user] $text" }
      .evalMap(chatRoom.publish)

  val routes: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case GET -> Root =>
      Ok(indexPage).map(_.withContentType(`Content-Type`(MediaType.text.html)))

    case GET -> Root / "health" =>
      Ok("ok")

    case GET -> Root / "ws" / user if user.nonEmpty =>
      val onDisconnect = chatRoom.publish(s"*** $user left ***")
      for
        _ <- chatRoom.publish(s"*** $user joined ***")
        webSocketBuilder <- WebSocketBuilder2[IO]
        response <- webSocketBuilder
          .build(send = chatRoom.frames.onFinalize(onDisconnect), receive = receive(user))
      yield response

  private val indexPage: String =
    """
      |<!DOCTYPE html>
      |<html lang="en">
      |<head>
      |  <meta charset="UTF-8" />
      |  <title>Scala WebSocket Chat</title>
      |</head>
      |<body>
      |  <h1>Scala WebSocket Chat</h1>
      |  <input id="name" placeholder="Your name" />
      |  <button id="connect">Connect</button>
      |  <br/><br/>
      |  <input id="message" placeholder="Type a message" />
      |  <button id="send">Send</button>
      |  <pre id="log"></pre>
      |  <script>
      |    let socket;
      |    const log = (m) => {
      |      const el = document.getElementById('log');
      |      el.textContent += m + '\n';
      |    };
      |    document.getElementById('connect').onclick = () => {
      |      const name = document.getElementById('name').value.trim() || 'guest';
      |      socket = new WebSocket(`ws://${window.location.host}/ws/${encodeURIComponent(name)}`);
      |      socket.onmessage = (event) => log(event.data);
      |      socket.onopen = () => log('connected');
      |      socket.onclose = () => log('disconnected');
      |    };
      |    document.getElementById('send').onclick = () => {
      |      if (!socket || socket.readyState !== WebSocket.OPEN) return;
      |      const input = document.getElementById('message');
      |      socket.send(input.value);
      |      input.value = '';
      |    };
      |  </script>
      |</body>
      |</html>
      |""".stripMargin
