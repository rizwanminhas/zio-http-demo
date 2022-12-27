import zio.{ZIO, ZIOAppDefault}
import zio.*
import zhttp.http.*
import zhttp.service.Server

import java.lang
import scala.util.control

object ZIOHTTP extends ZIOAppDefault:

  val port = 9000

  val app: Http[Any, Nothing, Request, Response] = Http.collect[Request] {
    case Method.GET -> !! / "greet" => Response.text("hello rizwan")
  }

  val zApp: UHttpApp = Http.collectZIO[Request] {
    case Method.POST -> !! / "greet" =>
      Random.nextIntBetween(3,5).map(n => Response.text(s"${"Hello" * n} rizwan!"))
  }

  // ++ (concatenation) means if doesn't match on left side then try right side. If left side matches and fails then the right side will NOT be tried.
  // <> (orElse) means if left fails then right will be tried.
  val combined = app ++ zApp

  // middleware
  val wrapped = combined @@ Middleware.debug
  // request --> middleware --> combined

  val loggingHttp = combined @@ Verbose.log


  val httpProgram = for {
    _ <- Console.printLine(s"Starting server at http://localhost:$port")
    _ <- Server.start(port, loggingHttp)
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