package back
package scripts

import bleep.commands.Dist
import bleep.model.{CrossProjectName, ProjectName}
import bleep.{BleepScript, Commands, PathOps, Started, cli}

import java.nio.file.Files

object EdgeDbMigrate extends BleepScript("EdgeDbMigrate") {
  private def getEnvOrThrow(env: String): String                                          = sys.env.getOrElse(env, throw new RuntimeException(s"$env is not defined"))
  private def getDevOrProdEnv(gitBranch: String)(prodEnv: String, devEnv: String): String = if gitBranch == "origin/main" then
    getEnvOrThrow(prodEnv)
  else getEnvOrThrow(devEnv)

  override def run(started: Started, commands: Commands, args: List[String]): Unit =
    started.logger.info("This script is here to run EdgeDb migrations.")

  cli(
    "Docker login",
    tempDir,
    List("docker", "login", "docker.ilieff.fr", "-u charles", s"-p$DOCKER_REPO_PASSWORD"),
    logger = started.logger,
    out = cli.Out.ViaLogger(started.logger),
    env = Nil
  )

  val GIT_BRANCH                   = getEnvOrThrow("GIT_BRANCH")
  def getDevOrProdEnvWithGitBranch = getDevOrProdEnv(GIT_BRANCH)
  val SMTP_PASSWORD                = getDevOrProdEnvWithGitBranch("SMTP_PASSWORD_PROD", "SMTP_PASSWORD")

  val SMTP_LOGIN = getDevOrProdEnvWithGitBranch("SMTP_LOGIN_PROD", "SMTP_LOGIN")
  val SMTP_URL   = getDevOrProdEnvWithGitBranch("SMTP_URL_PROD", "SMTP_URL")

  val DATABASE_URL =
    if GIT_BRANCH == "origin/main" then "jdbc:mysql://192.168.1.7:3306/gestionhebergement"
    else "jdbc:mysql://192.168.1.3:3306/gestionhebergement_test"

  started.logger.info(s"Data base url is $DATABASE_URL")

  val SECRET_KEY        = getEnvOrThrow("SECRET_KEY")
  val DATABASE_PASSWORD = getEnvOrThrow("DATABASE_PASSWORD")
  val ACTIVE_PROFILE    = "prod"

  val dockerFile =
    s"""
       |FROM openjdk:17 as campus-back-end
       |ENV SMTP_PASSWORD $SMTP_PASSWORD
       |ENV SMTP_LOGIN $SMTP_LOGIN
       |ENV SMTP_URL $SMTP_URL
       |ENV DATABASE_URL $DATABASE_URL
       |ENV DATABASE_PASSWORD $DATABASE_PASSWORD
       |ENV SECRET_KEY $SECRET_KEY
       |ENV ACTIVE_PROFILE $ACTIVE_PROFILE
       |
       |COPY /dist /usr/share/dist
       |# run
       |EXPOSE 8080
       |CMD ["/usr/share/dist/bin/campus-back"]
       |""".stripMargin

  Files.writeString(tempDir / "Dockerfile", dockerFile)

  val DOCKER_IMAGE_NAME = if GIT_BRANCH == "origin/main" then "campus-back" else "campus-back-dev"
  cli(
    "Docker build",
    tempDir,
    List("docker", "build", ".", "-t", s"docker.ilieff.fr/$DOCKER_IMAGE_NAME:1.3.0", "-t", s"docker.ilieff.fr/$DOCKER_IMAGE_NAME:latest"),
    logger = started.logger,
    out = cli.Out.ViaLogger(started.logger),
    env = Nil
  )

  started.logger.info(s"Docker pushing...")
  cli(
    "Docker pushing...",
    tempDir,
    List("docker", "push", "--all-tags", s"docker.ilieff.fr/$DOCKER_IMAGE_NAME"),
    logger = started.logger,
    out = cli.Out.ViaLogger(started.logger),
    env = Nil
  )
}
