package adapters

import com.edgedb.driver.{EdgeDBClient, EdgeDBConnection, TLSSecurityMode}
import zio.*

import scala.io.Source.fromFile
import scala.jdk.CollectionConverters.*

case class EdgeDbDriverLive(database: String = "main") {
/*  val tlsCAFromFile = fromFile("/home/carlos/.local/share/edgedb/data/backend/edbtlscert.pem").mkString*/
  var edgedbHost = sys.env.getOrElse("EDGEDB_HOST", "localhost")
  println("HOOOST"+edgedbHost)
  val connection    = EdgeDBConnection
    .builder()
    .withDatabase(
      database
    )
    .withHostname("edgedb")
    .withPort(5656)
/*    .withPort(10710)*/
    .withTlsSecurity(TLSSecurityMode.INSECURE)
/*    .withTlsca(tlsCAFromFile)*/
    .withUser("edgedb")
    .withPassword("password")
    //.withPassword("S4jPNBEu7Nkueb4Cwad2VA5h")
    .build()

  // Config and passwords can be found here :
  // val configPath    = Paths.get(ConfigUtils.getCredentialsDir, "backend" + ".json")

  private val client = new EdgeDBClient(connection)

  def querySingle[A](
    cls: Class[A],
    query: String
  ): Task[A] =
    ZIO
      .fromCompletionStage(
        client
          .querySingle(cls, query.stripMargin)
      ).flatMap {
        case null   => ZIO.fail(new NoSuchElementException(s"Query returned null: $query"))
        case result => ZIO.succeed(result)
      }.tapBoth(e => ZIO.logError(s"Query failed: $query" + e.getMessage), _ => ZIO.logInfo(s"Query succeeded: $query"))

  def query[A](
    cls: Class[A],
    query: String
  ): Task[List[A]] =
    ZIO
      .fromCompletionStage(
        client
          .query(cls, query.stripMargin)
          .thenApply(javaList => javaList.asScala.toList)
      )
}

object EdgeDbDriver {
  val layer: ULayer[EdgeDbDriverLive] = ZLayer.succeed(EdgeDbDriverLive())
  val testLayer: ULayer[EdgeDbDriverLive] = ZLayer.succeed(EdgeDbDriverLive("test"))
}
