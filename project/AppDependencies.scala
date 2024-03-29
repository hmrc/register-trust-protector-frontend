
import play.core.PlayVersion
import sbt._


object AppDependencies {

  val bootstrapVersion = "7.23.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-28"    % "8.5.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"       %% "domain"                        % "8.3.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.scalatest"          %% "scalatest"                % "3.2.18",
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"      %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.jsoup"              %  "jsoup"                    % "1.17.2",
    "com.typesafe.play"      %% "play-test"                % PlayVersion.current,
    "org.mockito"            %% "mockito-scala-scalatest"  % "1.17.30",
    "org.scalacheck"         %% "scalacheck"               % "1.17.0",
    "org.wiremock"            % "wiremock-standalone"      % "3.4.2",
    "io.github.wolfendale"   %% "scalacheck-gen-regexp"    % "1.1.0",

    "com.vladsch.flexmark"   %  "flexmark-all"             % "0.64.8"

  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

  val akkaVersion = "2.6.7"
  val akkaHttpVersion = "10.1.12"

  val overrides: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-stream_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor_2.12" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core_2.12" % akkaHttpVersion,
    "commons-codec"     % "commons-codec" % "1.12"
  )
}
