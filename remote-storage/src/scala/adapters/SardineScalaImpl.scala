package adapters

import com.github.sardine.*
import com.github.sardine.report.SardineReport
import org.w3c.dom.Element
import zio.*

import java.io.{File, InputStream}
import java.net.URL
import javax.xml.namespace.QName
import scala.jdk.CollectionConverters.*

case class SardineScalaImpl(path: String = "main") {
  private val NEXTCLOUD_USERNAME: String =
    sys.env.getOrElse("NEXTCLOUD_USERNAME", "none")

  private val NEXTCLOUD_PASSWORD: String =
    sys.env.getOrElse("NEXTCLOUD_PASSWORD", "none")

  private val NEXTCLOUD_URL =
    sys.env.getOrElse("NEXTCLOUD_URL", "https://nextcloud.ilieff.fr/remote.php/dav/files/carlosnextcloud/")

  val sardine = SardineFactory.begin(NEXTCLOUD_USERNAME, NEXTCLOUD_PASSWORD)

  def list: ZIO[Any, Throwable, List[DavResource]] = {
    val invoicePath = NEXTCLOUD_URL + "voiture/" + path
    for {

      _     <- ZIO.log(s"Listing remote webdav : $invoicePath")
      files <- ZIO.attempt(sardine.list(invoicePath).asScala.toList)
    } yield files
  }

  def setCredentials(username: String, password: String): Unit = ???

  def setCredentials(username: String, password: String, domain: String, workstation: String): Unit = ???

  def getResources(url: String): List[DavResource] = ???

  def list(url: String, depth: RuntimeFlags): List[DavResource] = ???

  def list(url: String, depth: RuntimeFlags, props: Set[QName]): List[DavResource] = ???

  def list(url: String, depth: RuntimeFlags, allProp: Boolean): List[DavResource] = ???

  def versionsList(url: String): List[DavResource] = ???

  def versionsList(url: String, depth: RuntimeFlags): List[DavResource] = ???

  def versionsList(url: String, depth: RuntimeFlags, props: Set[QName]): List[DavResource] = ???

  def propfind(url: String, depth: RuntimeFlags, props: Set[QName]): List[DavResource] = ???

  def report[T](url: String, depth: RuntimeFlags, report: SardineReport[T]): T = ???

  def search(url: String, language: String, query: String): List[DavResource] = ???

  def setCustomProps(url: String, addProps: Map[String, String], removeProps: List[String]): Unit = ???

  def patch(url: String, addProps: Map[QName, String]): List[DavResource] = ???

  def patch(url: String, addProps: Map[QName, String], removeProps: List[QName]): List[DavResource] = ???

  def patch(url: String, addProps: List[Element], removeProps: List[QName]): List[DavResource] = ???

  def patch(url: String, addProps: List[Element], removeProps: List[QName], headers: Map[String, String]): List[DavResource] = ???

  def get(url: String): InputStream = ???

  def get(url: String, version: String): InputStream = ???

  def get(url: String, headers: Map[String, String]): InputStream = ???

  def put(url: String, data: Array[Byte]): Unit = ???

  def put(url: String, dataStream: InputStream): Unit = ???

  def put(url: String, data: Array[Byte], contentType: String): Unit = ???

  def put(url: String, dataStream: InputStream, contentType: String): Unit = ???

  def put(url: String, dataStream: InputStream, contentType: String, expectContinue: Boolean): Unit = ???

  def put(url: String, dataStream: InputStream, contentType: String, expectContinue: Boolean, contentLength: Long): Unit = ???

  def put(url: String, dataStream: InputStream, headers: Map[String, String]): Unit = ???

  def put(url: String, localFile: File, contentType: String): Unit = ???

  def put(url: String, localFile: File, contentType: String, expectContinue: Boolean): Unit = ???

  def delete(url: String): Unit = ???

  def delete(url: String, headers: Map[String, String]): Unit = ???

  def createDirectory(url: String): Unit = ???

  def move(sourceUrl: String, destinationUrl: String): Unit = ???

  def move(sourceUrl: String, destinationUrl: String, overwrite: Boolean): Unit = ???

  def move(sourceUrl: String, destinationUrl: String, overwrite: Boolean, headers: Map[String, String]): Unit = ???

  def copy(sourceUrl: String, destinationUrl: String): Unit = ???

  def copy(sourceUrl: String, destinationUrl: String, overwrite: Boolean): Unit = ???

  def copy(sourceUrl: String, destinationUrl: String, overwrite: Boolean, headers: Map[String, String]): Unit = ???

  def exists(url: String): Boolean = ???

  def lock(url: String): String = ???

  def refreshLock(url: String, token: String, file: String): String = ???

  def unlock(url: String, token: String): Unit = ???

  def addToVersionControl(url: String): Unit = ???

  def checkout(url: String): Unit = ???

  def checkin(url: String): Unit = ???

  def getAcl(url: String): DavAcl = ???

  def getQuota(url: String): DavQuota = ???

  def setAcl(url: String, aces: List[DavAce]): Unit = ???

  def getPrincipals(url: String): List[DavPrincipal] = ???

  def getPrincipalCollectionSet(url: String): List[String] = ???

  def enableCompression(): Unit = ???

  def disableCompression(): Unit = ???

  def ignoreCookies(): Unit = ???

  def enablePreemptiveAuthentication(hostname: String): Unit = ???

  def enablePreemptiveAuthentication(url: URL): Unit = ???

  def enablePreemptiveAuthentication(hostname: String, httpPort: RuntimeFlags, httpsPort: RuntimeFlags): Unit = ???

  def disablePreemptiveAuthentication(): Unit = ???

  def shutdown(): Unit = ???
}

object SardineScalaImpl {
  val layer: ULayer[SardineScalaImpl]     = ZLayer.succeed(SardineScalaImpl())
  val testLayer: ULayer[SardineScalaImpl] = ZLayer.succeed(SardineScalaImpl("test"))
}
