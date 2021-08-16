/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import base.SpecBase
import connectors.SubmissionDraftConnector
import models.TaskStatus.InProgress
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.register.business.NamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustsStoreService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with BeforeAndAfterEach {

  private val name: String = "Company"
  private val utr: String = "1234567890"

  private val trustsStoreService: TrustsStoreService = mock[TrustsStoreService]
  private val submissionDraftConnector: SubmissionDraftConnector = mock[SubmissionDraftConnector]

  override def beforeEach(): Unit = {
    reset(trustsStoreService)

    when(trustsStoreService.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))
  }

  "Index Controller" when {

    "pre-existing user answers" must {

      "redirect to add-to page if there is at least one in-progress or completed protector" in {

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(NamePage(0), name).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
          .overrides(bind[SubmissionDraftConnector].toInstance(submissionDraftConnector))
          .build()

        when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
        when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))
        when(submissionDraftConnector.getTrustUtr(any())(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustBe controllers.register.routes.AddAProtectorController.onPageLoad(fakeDraftId).url

        verify(trustsStoreService, never()).updateTaskStatus(eqTo(draftId), any())(any(), any())

        application.stop()
      }

      "redirect to has-protectors page if there are no in-progress or completed protectors" in {

        val userAnswers: UserAnswers = emptyUserAnswers

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
          .overrides(bind[SubmissionDraftConnector].toInstance(submissionDraftConnector))
          .build()

        when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
        when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))
        when(submissionDraftConnector.getTrustUtr(any())(any(), any())).thenReturn(Future.successful(None))

        val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustBe controllers.register.routes.TrustHasProtectorYesNoController.onPageLoad(fakeDraftId).url

        verify(trustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(InProgress))(any(), any())

        application.stop()
      }

      "update value of is5mldEnabled, isTaxable and utr in user answers" in {

        reset(registrationsRepository)

        val userAnswers = emptyUserAnswers.copy(is5mldEnabled = false, isTaxable = false)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
          .overrides(bind[SubmissionDraftConnector].toInstance(submissionDraftConnector))
          .build()

        when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
        when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))
        when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(true))
        when(submissionDraftConnector.getTrustUtr(any())(any(), any())).thenReturn(Future.successful(Some(utr)))

        val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

        route(application, request).value.map { _ =>
          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

          uaCaptor.getValue.is5mldEnabled mustBe true
          uaCaptor.getValue.isTaxable mustBe true
          uaCaptor.getValue.existingTrustUtr.get mustBe utr

          application.stop()
        }
      }
    }

    "no pre-existing user answers" must {

      "instantiate new set of user answers" when {

        "5mld enabled" when {

          "taxable" must {

            "add is5mldEnabled = true and isTaxable = true value to user answers" in {

              reset(registrationsRepository)

              val application = applicationBuilder(userAnswers = None)
                .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
                .overrides(bind[SubmissionDraftConnector].toInstance(submissionDraftConnector))
                .build()

              when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
              when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
              when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))
              when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(true))
              when(submissionDraftConnector.getTrustUtr(any())(any(), any())).thenReturn(Future.successful(None))

              val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

              route(application, request).value.map { _ =>
                val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
                verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

                uaCaptor.getValue.is5mldEnabled mustBe true
                uaCaptor.getValue.isTaxable mustBe true
                uaCaptor.getValue.draftId mustBe fakeDraftId
                uaCaptor.getValue.internalAuthId mustBe "id"

                application.stop()
              }
            }
          }

          "non-taxable" must {

            "add is5mldEnabled = true and isTaxable = false value to user answers" in {

              reset(registrationsRepository)

              val application = applicationBuilder(userAnswers = None)
                .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
                .overrides(bind[SubmissionDraftConnector].toInstance(submissionDraftConnector))
                .build()

              when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
              when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
              when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))
              when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))
              when(submissionDraftConnector.getTrustUtr(any())(any(), any())).thenReturn(Future.successful(None))

              val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

              route(application, request).value.map { _ =>
                val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
                verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

                uaCaptor.getValue.is5mldEnabled mustBe true
                uaCaptor.getValue.isTaxable mustBe false
                uaCaptor.getValue.draftId mustBe fakeDraftId
                uaCaptor.getValue.internalAuthId mustBe "id"

                application.stop()
              }
            }
          }
        }

        "5mld not enabled" when {

          "taxable" must {

            "add is5mldEnabled = false and isTaxable = true value to user answers" in {

              reset(registrationsRepository)

              val application = applicationBuilder(userAnswers = None)
                .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
                .overrides(bind[SubmissionDraftConnector].toInstance(submissionDraftConnector))
                .build()

              when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
              when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
              when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
              when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(true))
              when(submissionDraftConnector.getTrustUtr(any())(any(), any())).thenReturn(Future.successful(None))

              val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

              route(application, request).value.map { _ =>
                val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
                verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

                uaCaptor.getValue.is5mldEnabled mustBe false
                uaCaptor.getValue.isTaxable mustBe true
                uaCaptor.getValue.draftId mustBe fakeDraftId
                uaCaptor.getValue.internalAuthId mustBe "id"

                application.stop()
              }
            }
          }

          "non-taxable" must {

            "add is5mldEnabled = false and isTaxable = false value to user answers" in {

              reset(registrationsRepository)

              val application = applicationBuilder(userAnswers = None)
                .overrides(bind[TrustsStoreService].toInstance(trustsStoreService))
                .overrides(bind[SubmissionDraftConnector].toInstance(submissionDraftConnector))
                .build()

              when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
              when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
              when(trustsStoreService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
              when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))
              when(submissionDraftConnector.getTrustUtr(any())(any(), any())).thenReturn(Future.successful(None))

              val request = FakeRequest(GET, routes.IndexController.onPageLoad(fakeDraftId).url)

              route(application, request).value.map { _ =>
                val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
                verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

                uaCaptor.getValue.is5mldEnabled mustBe false
                uaCaptor.getValue.isTaxable mustBe false
                uaCaptor.getValue.draftId mustBe fakeDraftId
                uaCaptor.getValue.internalAuthId mustBe "id"

                application.stop()
              }
            }
          }
        }
      }
    }
  }
}
