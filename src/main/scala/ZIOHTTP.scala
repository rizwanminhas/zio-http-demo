import zio.{ZIO, ZIOAppDefault}
import zio.*
import zhttp.http.*
import zhttp.service.Server

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

  val httpProgram = for {
    _ <- Console.printLine(s"Starting server at http://localhost:$port")
    _ <- Server.start(port, combined)
  } yield ()

  override def run: ZIO[Any, Any, Any] = httpProgram

