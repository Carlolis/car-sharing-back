package adapters

import com.geldata.driver.{GelClientPool, GelConnection}
import zio.*

import scala.jdk.CollectionConverters.*

case class GelDriverLive(database: String = "main") {
  /*  val tlsCAFromFile = fromFile("/home/carlos/.local/share/edgedb/data/backend/edbtlscert.pem").mkString*/
  private val EDGEDB_DSN: String =
    sys.env.getOrElse("EDGEDB_DSN", s"edgedb://edgedb:password@localhost:10700/$database?tls_security=insecure")

  private val connection = GelConnection.builder().withDsn(EDGEDB_DSN).build()
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
  private val CI: String =
    sys.env.getOrElse("CI", "false")
  private var client     = new GelClientPool(connection)
  if (CI == "true")
    client = new GelClientPool()

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

object GelDriver {
  val layer: ULayer[GelDriverLive]     = ZLayer.succeed(GelDriverLive())
  val testLayer: ULayer[GelDriverLive] = ZLayer.succeed(GelDriverLive("test"))
}
