/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register

import base.SpecBase
import forms.{AddAProtectorFormProvider, YesNoFormProvider}
import generators.ModelGenerators
import models.Status.{Completed, InProgress}
import models.register.pages.AddAProtector
import models.register.pages.IndividualOrBusinessToAdd.Individual
import models.{FullName, Status, TaskStatus, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.entitystatus.BusinessProtectorStatus
import pages.register.business.{NamePage, UtrPage, UtrYesNoPage}
import pages.register.{AddAProtectorPage, IndividualOrBusinessPage, TrustHasProtectorYesNoPage, business => bus, individual => ind}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import services.TrustsStoreService
import uk.gov.hmrc.http.HttpResponse
import utils.RegistrationProgress
import viewmodels.AddRow
import views.html.register.{AddAProtectorView, TrustHasProtectorYesNoView}

import scala.concurrent.Future

class AddAProtectorControllerSpec extends SpecBase with BeforeAndAfterEach with ScalaCheckPropertyChecks with ModelGenerators {

  private def onwardRoute: Call = Call("GET", "/foo")

  private val max: Int = 25

  private def removeBusinessRoute(index: Int): String =
    controllers.register.business.routes.RemoveProtectorController.onPageLoad(index, fakeDraftId).url

  private def changeBusinessRoute(index: Int): String =
    controllers.register.business.routes.CheckDetailsController.onPageLoad(index, fakeDraftId).url

  private lazy val addAProtectorRoute = routes.AddAProtectorController.onPageLoad(fakeDraftId).url

  private lazy val addOnePostRoute = routes.AddAProtectorController.submitOne(fakeDraftId).url

  private lazy val addAnotherPostRoute = routes.AddAProtectorController.submitAnother(fakeDraftId).url

  private lazy val submitCompleteRoute = routes.AddAProtectorController.submitComplete(fakeDraftId).url

  private val formProvider = new AddAProtectorFormProvider()
  private val form = formProvider()

  private val yesNoForm = new YesNoFormProvider().withPrefix("trustHasProtectorYesNo")

  private lazy val protectorsComplete = List(
    AddRow("Business Name 1", typeLabel = "Business protector", changeBusinessRoute(0), removeBusinessRoute(0)),
    AddRow("Business Name 2", typeLabel = "Business protector", changeBusinessRoute(1), removeBusinessRoute(1)),
    AddRow("Business Name 3", typeLabel = "Business protector", changeBusinessRoute(2), removeBusinessRoute(2))
  )

  private val userAnswersWithProtectorsComplete = emptyUserAnswers
    .set(TrustHasProtectorYesNoPage, true).success.value
    .set(NamePage(0), "Business Name 1").success.value
    .set(UtrYesNoPage(0), true).success.value
    .set(UtrPage(0), "1234567890").success.value
    .set(BusinessProtectorStatus(0), Completed).success.value
    .set(NamePage(1), "Business Name 2").success.value
    .set(UtrYesNoPage(1), true).success.value
    .set(UtrPage(1), "1234567890").success.value
    .set(BusinessProtectorStatus(1), Completed).success.value
    .set(NamePage(2), "Business Name 3").success.value
    .set(UtrYesNoPage(2), true).success.value
    .set(UtrPage(2), "1234567890").success.value
    .set(BusinessProtectorStatus(2), Completed).success.value

  private def genBusinessProtectors(range: Int): UserAnswers = {
    (0 until range)
      .foldLeft(emptyUserAnswers)((ua,index) =>
        ua.set(bus.NamePage(index), "Business Name").success.value
      )
  }

  private def genIndividualProtectors(range: Int): UserAnswers = {
    (0 until range)
      .foldLeft(emptyUserAnswers)((ua,index) =>
        ua.set(ind.NamePage(index), FullName("First", None, "Last")).success.value
      )
  }

  private val mockTrustsStoreService: TrustsStoreService = mock[TrustsStoreService]
  private val mockRegistrationProgress: RegistrationProgress = mock[RegistrationProgress]

  override def beforeEach(): Unit = {
    reset(mockTrustsStoreService, mockRegistrationProgress)

    when(mockTrustsStoreService.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))
  }

  "AddAProtector Controller" when {

    "no data" must {

      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, addAProtectorRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(POST, addAnotherPostRoute)
          .withFormUrlEncodedBody(("value", AddAProtector.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }
    }

    "there are no protectors" must {

      "return OK and the correct view for a GET" in {

        val userAnswers = emptyUserAnswers
          .set(TrustHasProtectorYesNoPage, true).success.value
          .set(IndividualOrBusinessPage, Individual).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, addAProtectorRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TrustHasProtectorYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, fakeDraftId)(request, messages).toString

        val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(registrationsRepository).set(uaCaptor.capture)(any(), any())
        uaCaptor.getValue.get(IndividualOrBusinessPage) mustNot be(defined)

        application.stop()
      }

      "redirect to the next page when valid data is submitted" when {

        "yes selected" in {

          val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, false).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustsStoreService].toInstance(mockTrustsStoreService))
            .build()

          val request = FakeRequest(POST, addOnePostRoute)
            .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockTrustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(TaskStatus.InProgress))(any(), any())

          application.stop()
        }

        "no selected" in {

          val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, false).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustsStoreService].toInstance(mockTrustsStoreService))
            .build()

          val request = FakeRequest(POST, addOnePostRoute)
            .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url

          verify(mockTrustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(TaskStatus.Completed))(any(), any())

          application.stop()
        }
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, true).success.value

        val application = applicationBuilder(Some(userAnswers)).build()

        val request = FakeRequest(POST, addOnePostRoute)
          .withFormUrlEncodedBody(("value", ""))

        val boundForm = yesNoForm.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[TrustHasProtectorYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, fakeDraftId)(request, messages).toString

        application.stop()
      }

    }

    "there are protectors" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithProtectorsComplete)).build()

        val request = FakeRequest(GET, addAProtectorRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAProtectorView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, fakeDraftId, Nil, protectorsComplete, "You have added 3 protectors", Nil)(request, messages).toString

        application.stop()
      }

      "populate the view without value on a GET when the question has previously been answered" in {

        val userAnswers = userAnswersWithProtectorsComplete
          .set(AddAProtectorPage, AddAProtector.YesNow).success.value
          .set(TrustHasProtectorYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, addAProtectorRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAProtectorView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, fakeDraftId, Nil, protectorsComplete, "You have added 3 protectors", Nil)(request, messages).toString

        application.stop()
      }

      "redirect to the next page when valid data is submitted" when {

        "YesNow selected" in {

          val selection = AddAProtector.YesNow

          when(mockRegistrationProgress.protectorsStatus(any())).thenReturn(Some(InProgress))

          val application = applicationBuilder(userAnswers = Some(userAnswersWithProtectorsComplete))
            .overrides(bind[TrustsStoreService].toInstance(mockTrustsStoreService))
            .overrides(bind[RegistrationProgress].toInstance(mockRegistrationProgress))
            .build()

          val request = FakeRequest(POST, addAnotherPostRoute)
            .withFormUrlEncodedBody(("value", selection.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

          verify(mockTrustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(TaskStatus.InProgress))(any(), any())
          verify(mockRegistrationProgress).protectorsStatus(eqTo(userAnswersWithProtectorsComplete.set(AddAProtectorPage, selection).success.value))

          application.stop()
        }

        "YesLater selected" in {

          val selection = AddAProtector.YesLater

          when(mockRegistrationProgress.protectorsStatus(any())).thenReturn(Some(InProgress))

          val application = applicationBuilder(userAnswers = Some(userAnswersWithProtectorsComplete))
            .overrides(bind[TrustsStoreService].toInstance(mockTrustsStoreService))
            .overrides(bind[RegistrationProgress].toInstance(mockRegistrationProgress))
            .build()

          val request = FakeRequest(POST, addAnotherPostRoute)
            .withFormUrlEncodedBody(("value", selection.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

          verify(mockTrustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(TaskStatus.InProgress))(any(), any())
          verify(mockRegistrationProgress).protectorsStatus(eqTo(userAnswersWithProtectorsComplete.set(AddAProtectorPage, selection).success.value))

          application.stop()
        }

        "NoComplete selected" when {

          val selection = AddAProtector.NoComplete

          "registration progress is completed" in {

            when(mockRegistrationProgress.protectorsStatus(any())).thenReturn(Some(Completed))

            val application = applicationBuilder(userAnswers = Some(userAnswersWithProtectorsComplete))
              .overrides(bind[TrustsStoreService].toInstance(mockTrustsStoreService))
              .overrides(bind[RegistrationProgress].toInstance(mockRegistrationProgress))
              .build()

            val request = FakeRequest(POST, addAnotherPostRoute)
              .withFormUrlEncodedBody(("value", selection.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

            verify(mockTrustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(TaskStatus.Completed))(any(), any())
            verify(mockRegistrationProgress).protectorsStatus(eqTo(userAnswersWithProtectorsComplete.set(AddAProtectorPage, selection).success.value))

            application.stop()
          }

          "registration progress is not completed" in {

            forAll(arbitrary[Option[Status]].suchThat(!_.contains(Completed))) { regProgressStatus =>
              beforeEach()

              when(mockRegistrationProgress.protectorsStatus(any())).thenReturn(regProgressStatus)

              val application = applicationBuilder(userAnswers = Some(userAnswersWithProtectorsComplete))
                .overrides(bind[TrustsStoreService].toInstance(mockTrustsStoreService))
                .overrides(bind[RegistrationProgress].toInstance(mockRegistrationProgress))
                .build()

              val request = FakeRequest(POST, addAnotherPostRoute)
                .withFormUrlEncodedBody(("value", selection.toString))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

              verify(mockTrustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(TaskStatus.InProgress))(any(), any())
              verify(mockRegistrationProgress).protectorsStatus(eqTo(userAnswersWithProtectorsComplete.set(AddAProtectorPage, selection).success.value))

              application.stop()
            }
          }
        }
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(POST, addAnotherPostRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddAProtectorView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, fakeDraftId, Nil, Nil, "Add a protector", Nil)(request, messages).toString

        application.stop()
      }

    }

    "maxed out protectors" must {

      "return correct view when individual protectors are maxed out" in {

        val protectors = List(
          genIndividualProtectors(max)
        )

        val userAnswers = protectors.foldLeft(emptyUserAnswers)((x, acc) => acc.copy(data = x.data.deepMerge(acc.data)))
          .set(TrustHasProtectorYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, addAProtectorRoute)

        val result = route(application, request).value

        contentAsString(result) must include("You cannot add another individual as you have entered a maximum of 25.")
        contentAsString(result) must include(messages("addAProtector.maxedOut.all.paragraph"))

        application.stop()
      }

      "return correct view when business protectors are maxed out" in {

        val protectors = List(
          genBusinessProtectors(max)
        )

        val userAnswers = protectors.foldLeft(emptyUserAnswers)((x, acc) => acc.copy(data = x.data.deepMerge(acc.data)))
          .set(TrustHasProtectorYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, addAProtectorRoute)

        val result = route(application, request).value

        contentAsString(result) must include("You cannot add another business as you have entered a maximum of 25.")
        contentAsString(result) must include(messages("addAProtector.maxedOut.paragraph"))

        application.stop()
      }

      "return correct view when all protectors are maxed out" in {

        val protectors = List(
          genIndividualProtectors(max),
          genBusinessProtectors(max)
        )

        val userAnswers = protectors.foldLeft(emptyUserAnswers)((x, acc) => acc.copy(data = x.data.deepMerge(acc.data)))
          .set(TrustHasProtectorYesNoPage, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, addAProtectorRoute)

        val result = route(application, request).value

        contentAsString(result) must include("You cannot enter another protector as you have entered a maximum of 50.")
        contentAsString(result) must include(messages("addAProtector.maxedOut.all.paragraph"))

        application.stop()
      }

      "redirect to registration progress when user clicks continue" when {

        "registration progress is completed" in {

          when(mockRegistrationProgress.protectorsStatus(any())).thenReturn(Some(Completed))

          val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[TrustsStoreService].toInstance(mockTrustsStoreService))
            .overrides(bind[RegistrationProgress].toInstance(mockRegistrationProgress))
            .build()

          val request = FakeRequest(POST, submitCompleteRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual "http://localhost:9781/trusts-registration/draftId/registration-progress"

          verify(mockTrustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(TaskStatus.Completed))(any(), any())
          verify(mockRegistrationProgress).protectorsStatus(eqTo(userAnswers.set(AddAProtectorPage, AddAProtector.NoComplete).success.value))

          application.stop()
        }

        "registration progress is not completed" in {

          forAll(arbitrary[Option[Status]].suchThat(!_.contains(Completed))) { regProgressStatus =>
            beforeEach()

            when(mockRegistrationProgress.protectorsStatus(any())).thenReturn(regProgressStatus)

            val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(bind[TrustsStoreService].toInstance(mockTrustsStoreService))
              .overrides(bind[RegistrationProgress].toInstance(mockRegistrationProgress))
              .build()

            val request = FakeRequest(POST, submitCompleteRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual "http://localhost:9781/trusts-registration/draftId/registration-progress"

            verify(mockTrustsStoreService).updateTaskStatus(eqTo(draftId), eqTo(TaskStatus.InProgress))(any(), any())
            verify(mockRegistrationProgress).protectorsStatus(eqTo(userAnswers.set(AddAProtectorPage, AddAProtector.NoComplete).success.value))

            application.stop()
          }
        }
      }
    }
  }
}
