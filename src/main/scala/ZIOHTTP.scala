import zio.{ZIO, ZIOAppDefault}
import zio.*
import zhttp.http.*
import zhttp.http.middleware.Cors.CorsConfig
import zhttp.service.ChannelEvent.{ChannelRead, ChannelUnregistered, UserEventTriggered}
import zhttp.service.ChannelEvent.UserEvent.{HandshakeComplete, HandshakeTimeout}
import zhttp.service.{ChannelEvent, Server}
import zhttp.socket.{WebSocketChannelEvent, WebSocketFrame}

import java.lang
import scala.util.control

object ZIOHTTP extends ZIOAppDefault:

  val port = 9000

  val app: Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> !! / "greet" => Response.text("hello rizwan")
  } @@ Middleware.csrfGenerate()

  val zApp: UHttpApp = Http.collectZIO[Request] {
    case Method.POST -> !! / "greet" =>
      Random.nextIntBetween(3,5).map(n => Response.text(s"${"Hello" * n} rizwan!"))
  } @@ Middleware.csrfValidate()

  val authApp: UHttpApp = Http.collect[Request] {
    case Method.GET -> !! / "secret" =>
      Response.text("The password is minhas")
  } @@ Middleware.basicAuth("rizwan", "minhas")

  // ++ (concatenation) means if doesn't match on left side then try right side. If left side matches and fails then the right side will NOT be tried.
  // <> (orElse) means if left fails then right will be tried.
  val combined = app ++ zApp

  // middleware
  val wrapped = combined @@ Middleware.debug
  // request --> middleware --> combined

  val loggingHttp = combined @@ Verbose.log

  val corsConfig = CorsConfig(
    anyOrigin = false,
    anyMethod = false,
    allowedOrigins = s => s.equals("localhost"),
    allowedMethods = Some(Set(Method.GET, Method.POST))
  )

  val corsEnabledHttp = combined @@ Middleware.cors(corsConfig) @@ Verbose.log

  val sarcastic: String => String = txt => txt.toList.zipWithIndex.map {
    case (c: Char, i: Int) => if (i % 2 == 0) c.toUpper else c.toLower
  }.mkString

  val wsLogic: Http[Any, Throwable, WebSocketChannelEvent, Unit] =
    Http.collectZIO[WebSocketChannelEvent] {
      case ChannelEvent(channel, ChannelRead(WebSocketFrame.Text(message))) =>
        channel.writeAndFlush(WebSocketFrame.text(sarcastic(message)))
      case ChannelEvent(channel, UserEventTriggered(event)) =>
        event match
          case HandshakeComplete => Console.printLine("Websocket started")
          case HandshakeTimeout => Console.printLine("Handshake timed out")
      case ChannelEvent(channel, ChannelUnregistered) => Console.printLine("Channel unregistered")
    }

  val wsApp = Http.collectZIO[Request] {
    case Method.GET -> !! / "chat" => wsLogic.toSocketApp.toResponse
  }

  val httpProgram = for {
    _ <- Console.printLine(s"Starting server at http://localhost:$port")
    _ <- Server.start(port, wsApp)
  } yield ()
  
  override def run: ZIO[Any, Any, Any] = httpProgram

// request => contramap => http application => response => map => final response
//         |-------------------- middleware --------------------|
object Verbose {
  def log[R, E >: Exception]:
  Middleware[R, E, Request, Response, Request, Response] = new Middleware[R, E, Request, Response, Request, Response] {
    override def apply[R1 <: R, E1 >: E](http: Http[R1, E1, Request, Response]): Http[R1, E1, Request, Response] =
      http
        .contramapZIO[R1, E1, Request](request =>
          for {
            _ <- Console.printLine(s"> ${request.method} ${request.path} ${request.version}")
            _ <- ZIO.foreach(request.headers.toList) { header =>
              Console.printLine(s"> ${header._1} ${header._2}")
            }
          } yield request
        )
        .mapZIO[R1, E1, Response](response =>
          for {
            _ <- Console.printLine(s"< ${response.status}")
            _ <- ZIO.foreach(response.headers.toList) { header =>
              Console.printLine(s"< ${header._1} ${header._2}")
            }
          } yield response
      )
  }
}