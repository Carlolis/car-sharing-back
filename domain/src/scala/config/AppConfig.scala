package config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.TypesafeConfigProvider

/**
 * Main application configuration that holds all configuration components
 */
final case class AppConfig(
  webdav: WebDavConfig,
  auth: AuthConfig
)

/**
 * Configuration for authentication service
 */
private case class AuthConfig(
  secretKey: String,
  tokenExpirationSeconds: Long
)

private case class WebDavConfig(
  username: String,
  password: String,
  baseUrl: String,
  basePath: String
)

object AppConfig:
  val layer =
    ZLayer.fromZIO(TypesafeConfigProvider.fromResourcePath().load(deriveConfig[AppConfig]))
