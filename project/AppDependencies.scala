import play.core.PlayVersion
import sbt._

object AppDependencies {

  val bootstrapVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "8.5.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc"       %% "domain-play-30"                        % "9.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.scalatestplus"      %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.jsoup"              %  "jsoup"                    % "1.17.2",
    "org.mockito"            %% "mockito-scala-scalatest"  % "1.17.31",
    "io.github.wolfendale"   %% "scalacheck-gen-regexp"    % "1.1.0",
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
