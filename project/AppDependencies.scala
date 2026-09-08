import sbt.*

object AppDependencies {

  val playBootstrapVersion = "10.8.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30"            % playBootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30"            % "13.13.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping-play-30" % "3.5.0",
    "uk.gov.hmrc" %% "domain-play-30"                        % "11.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30" % playBootstrapVersion,
    "org.scalatestplus"    %% "scalacheck-1-19"        % "3.2.19.0",
    "io.github.wolfendale" %% "scalacheck-gen-regexp"  % "1.1.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
