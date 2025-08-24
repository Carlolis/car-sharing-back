package adapters

import com.github.sardine.*
import config.{AppConfig, WebDavConfig}
import zio.*

import java.io.InputStream
import scala.jdk.CollectionConverters.*

case class SardineScalaImpl(appConfig: AppConfig) {
  private val config      = appConfig.webdav
  private val directory   = config.directory
  private val invoicePath = config.baseUrl + config.basePath + "/" + directory + "/"

  // Create a Sardine instance with proper resource management
  private def makeSardine: ZIO[Scope, Throwable, Sardine] = ZIO.acquireRelease {
    ZIO.attempt {
      ZIO.log(s"Connecting to ${config.baseUrl} with username ${config.username}")
      val s = SardineFactory.begin(config.username, config.password)
      s.enablePreemptiveAuthentication(config.baseUrl)
      s
    }
  } { sardine =>
    ZIO
      .attempt(sardine.shutdown())
      .tapBoth(
        e => ZIO.logError(s"Error closing Sardine connection: ${e.getMessage}"),
        _ => ZIO.log("Sardine connection closed successfully")
      )
      .ignore
  }

  def list: ZIO[Scope, Throwable, List[DavResource]] =
    for {
      _       <- ZIO.log(s"Listing remote webdav : $invoicePath")
      sardine <- makeSardine
      files   <- ZIO.attempt(sardine.list(invoicePath).asScala.toList)
    } yield files

  def put(dataStream: InputStream, contentType: String, fileName: String): ZIO[Scope, Throwable, Unit] =
    for {
      _       <- ZIO.log(s"Uploading file to WebDAV: $invoicePath")
      sardine <- makeSardine
      _       <- ZIO.attempt {
                   if (dataStream == null) throw new IllegalArgumentException("DataStream cannot be null")

                   // Always buffer the input stream to ensure it's repeatable
                   val bytes = dataStream.readAllBytes()
                   if (bytes.isEmpty) throw new IllegalArgumentException("DataStream is empty")

                   // Use the simplest put method with byte array
                   sardine.put(invoicePath + fileName, bytes)
                 }
      _       <- ZIO.log("File uploaded successfully")
    } yield ()

  def put(dataStream: Array[Byte], contentType: String, fileName: String): ZIO[Scope, Throwable, Unit] =
    for {
      _       <- ZIO.log(s"Uploading file to WebDAV: $invoicePath")
      sardine <- makeSardine
      _       <- ZIO.attempt {
                   if (dataStream == null) throw new IllegalArgumentException("DataStream cannot be null")
                   if (dataStream.isEmpty) throw new IllegalArgumentException("DataStream is empty")

                   // Use the simplest put method with byte array
                   sardine.put(invoicePath + fileName, dataStream)
                 }
      _       <- ZIO.log("File uploaded successfully")
    } yield ()

  def delete(fileName: String): ZIO[Scope, Throwable, Unit] = for {
    _       <- ZIO.log(s"Deleting file from WebDAV: $invoicePath$fileName")
    sardine <- makeSardine
    _       <- ZIO.attempt {
                 sardine.delete(invoicePath + fileName)
               }
    _       <- ZIO.log("File deleted successfully")
  } yield ()

  def get(fileName: String): ZIO[Scope, Throwable, InputStream] = for {
    _       <- ZIO.log(s"Downloading file from WebDAV: $invoicePath$fileName")
    sardine <- makeSardine
    file    <- ZIO.attempt {
                 sardine.get(invoicePath + fileName)
               }
    _       <- ZIO.log("File downloaded successfully")
  } yield file
}

object SardineScalaImpl {
  val layer: ZLayer[AppConfig, Nothing, SardineScalaImpl] =
    ZLayer.fromFunction(appConfig => SardineScalaImpl(appConfig))

  // Helper methods to use with ZIO.scoped
  def list: ZIO[Scope & SardineScalaImpl, Throwable, List[DavResource]] =
    ZIO.serviceWithZIO[SardineScalaImpl](_.list)

  def put(dataStream: Array[Byte], contentType: String, fileName: String): ZIO[Scope & SardineScalaImpl, Throwable, Unit] =
    ZIO.serviceWithZIO[SardineScalaImpl](_.put(dataStream, contentType, fileName))

  def put(dataStream: InputStream, contentType: String, fileName: String): ZIO[Scope & SardineScalaImpl, Throwable, Unit] =
    ZIO.serviceWithZIO[SardineScalaImpl](_.put(dataStream, contentType, fileName))

  def delete(fileName: String): ZIO[Scope & SardineScalaImpl, Throwable, Unit] =
    ZIO.serviceWithZIO[SardineScalaImpl](_.delete(fileName))

  def get(fileName: String): ZIO[Scope & SardineScalaImpl, Throwable, InputStream] =
    ZIO.serviceWithZIO[SardineScalaImpl](_.get(fileName))
}
