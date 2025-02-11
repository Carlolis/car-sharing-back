package adapters

import com.edgedb.driver.{EdgeDBClient, EdgeDBConnection, TLSSecurityMode}
import zio.*

import scala.io.Source.fromFile
import scala.jdk.CollectionConverters.*

case class EdgeDbDriverLive(database: String = "main") {
/*  val tlsCAFromFile = fromFile("/home/carlos/.local/share/edgedb/data/backend/edbtlscert.pem").mkString*/


  private var EDGEDB_DSN: String = sys.env.getOrElse("EDGEDB_DSN", s"edgedb://edgedb:password@localhost:10700/$database?tls_security=insecure")

  private val connection    = EdgeDBConnection.fromDSN(EDGEDB_DSN)
   /* .builder()
    .withDatabase(
      database
    )
    .withHostname("192.168.1.108")
    .withPort(5656)
/*    .withPort(10710)*/
    .withTlsSecurity(TLSSecurityMode.INSECURE)
/*    .withTlsca(tlsCAFromFile)*/
    .withUser("edgedb")
    .withPassword("password")
    //.withPassword("S4jPNBEu7Nkueb4Cwad2VA5h")
    .build()*/

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
