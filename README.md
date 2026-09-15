# Otus-Scala-Final

Educational Scala project: a minimal websocket chat for web.

## Run

```bash
sbt run
```

Then open http://localhost:8080 in your browser.

## WebSocket endpoint

- `GET /ws/{username}` — connect to the chat
- `GET /health` — health check
