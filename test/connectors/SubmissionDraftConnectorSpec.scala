/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import models.{RegistrationSubmission, SubmissionDraftResponse}
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsBoolean, JsString, Json}
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

import java.time.LocalDateTime
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SubmissionDraftConnectorSpec extends SpecBase with Matchers with OptionValues with WireMockHelper {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      defaultAppConfigurations ++ Seq("microservice.services.trusts.port" -> server.port()): _*
    ).build()

  private lazy val connector = injector.instanceOf[SubmissionDraftConnector]

  private val testDraftId = "draftId"
  private val testSection = "section"
  private val submissionsUrl = s"/trusts/register/submission-drafts"
  private val submissionUrl = s"$submissionsUrl/$testDraftId/$testSection"
  private val setSubmissionUrl = s"$submissionsUrl/$testDraftId/set/$testSection"

  "SubmissionDraftConnector" when {

    "submission drafts" must {

      "set data for section set" in {

        val sectionData = Json.parse(
          """
            |{
            | "field1": "value1",
            | "field2": "value2"
            |}
            |""".stripMargin)

        val submissionDraftSetData = RegistrationSubmission.DataSet(sectionData, List.empty, List.empty)

        server.stubFor(
          post(urlEqualTo(setSubmissionUrl))
            .withHeader(CONTENT_TYPE, containing("application/json"))
            .withRequestBody(equalTo(Json.toJson(submissionDraftSetData).toString()))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
            )
        )

        val result = Await.result(connector.setDraftSectionSet(testDraftId, testSection, submissionDraftSetData), Duration.Inf)
        result.status mustBe Status.OK
      }

      "get data for section" in {

        val draftData = Json.parse(
          """
            |{
            | "field1": "value1",
            | "field2": "value2"
            |}
            |""".stripMargin)

        val draftResponseJson =
          """
            |{
            | "createdAt": "2012-02-03T09:30:00",
            | "data": {
            |  "field1": "value1",
            |  "field2": "value2"
            | }
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(submissionUrl))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(draftResponseJson)
            )
        )

        val result: SubmissionDraftResponse = Await.result(connector.getDraftSection(testDraftId, testSection), Duration.Inf)
        result.createdAt mustBe LocalDateTime.of(2012, 2, 3, 9, 30)
        result.data mustBe draftData
      }
    }

    "getIsTrustTaxable" must {

      "return true if the trust is taxable" in {
        server.stubFor(
          get(urlEqualTo(s"$submissionsUrl/$testDraftId/is-trust-taxable"))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(JsBoolean(true).toString)
            )
        )

        val result: Boolean = Await.result(connector.getIsTrustTaxable(testDraftId), Duration.Inf)
        result.booleanValue() mustBe true
      }

      "return false if the trust is non taxable" in {
        server.stubFor(
          get(urlEqualTo(s"$submissionsUrl/$testDraftId/is-trust-taxable"))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(JsBoolean(false).toString)
            )
        )

        val result: Boolean = Await.result(connector.getIsTrustTaxable(testDraftId), Duration.Inf)
        result.booleanValue() mustBe false
      }

      "recover to true as default" in {
        server.stubFor(
          get(urlEqualTo(s"$submissionsUrl/$testDraftId/is-trust-taxable"))
            .willReturn(
              aResponse()
                .withStatus(Status.NOT_FOUND)
            )
        )

        val result: Boolean = Await.result(connector.getIsTrustTaxable(testDraftId), Duration.Inf)
        result.booleanValue() mustBe true
      }
    }

    "getTrustUtr" must {

      val utr = "1234567890"

      "return utr if utr found in submission data" in {
        server.stubFor(
          get(urlEqualTo(s"$submissionsUrl/$testDraftId/trust-utr"))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(JsString(utr).toString)
            )
        )

        val result: Option[String] = Await.result(connector.getTrustUtr(testDraftId), Duration.Inf)
        result mustBe Some(utr)
      }

      "return None if utr not found in submission data" in {
        server.stubFor(
          get(urlEqualTo(s"$submissionsUrl/$testDraftId/trust-utr"))
            .willReturn(
              aResponse()
                .withStatus(Status.NOT_FOUND)
            )
        )

        val result: Option[String] = Await.result(connector.getTrustUtr(testDraftId), Duration.Inf)
        result mustBe None
      }
    }
  }
}
