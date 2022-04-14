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

package navigation

import base.SpecBase
import config.FrontendAppConfig
import controllers.register.business.{routes => brts}
import controllers.register.individual.{routes => irts}
import controllers.register.{routes => rts}
import generators.Generators
import models.Status.Completed
import models.register.pages.{AddAProtector, IndividualOrBusinessToAdd}
import models.{FullName, Status, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.entitystatus.{BusinessProtectorStatus, IndividualProtectorStatus}
import pages.register.{business => bus, individual => ind, _}
import play.api.mvc.Call

class ProtectorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val max: Int = 25

  private def protectorsCompletedRoute(draftId: String, config: FrontendAppConfig): Call = {
    Call("GET", config.registrationProgressUrl(draftId))
  }

  val navigator: ProtectorNavigator = injector.instanceOf[ProtectorNavigator]

   "AnswersPage" when {
     "go to AddAProtectorPage from AnswersPage" in {
       forAll(arbitrary[UserAnswers]) {
         userAnswers =>
           navigator.nextPage(AnswersPage, fakeDraftId, userAnswers)
             .mustBe(controllers.register.routes.AddAProtectorController.onPageLoad(fakeDraftId))
       }
     }
   }


  "AddAProtectorYesNoPage" when {

    "go to IndividualOrBusinessPage from AddAProtectorYesNoPage when selected yes" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(AddAProtectorYesNoPage, true).success.value

          navigator.nextPage(AddAProtectorYesNoPage, fakeDraftId, answers)
            .mustBe(controllers.register.routes.IndividualOrBusinessController.onPageLoad(fakeDraftId))
      }

    }

    "go to RegistrationProgress from AddAProtectorYesNoPage when selected no" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(AddAProtectorYesNoPage, false).success.value

          navigator.nextPage(AddAProtectorYesNoPage, fakeDraftId, answers)
            .mustBe(protectorsCompletedRoute(fakeDraftId, frontendAppConfig))
      }
    }

    "go to IndividualOrBusinessPage from AddAProtectorPage when selected add them now" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(AddAProtectorPage, AddAProtector.YesNow).success.value

          navigator.nextPage(AddAProtectorPage, fakeDraftId, answers)
            .mustBe(controllers.register.routes.IndividualOrBusinessController.onPageLoad(fakeDraftId))
      }
    }

    "go to Individual name page from AddAProtectorPage when selected add them now and 25 business protectors" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = (0 until 25).foldLeft(userAnswers)((acc, index) => {
            acc
              .set(bus.NamePage(index), "Business Name").success.value
              .set(BusinessProtectorStatus(index), Completed).success.value
          }).set(AddAProtectorPage, AddAProtector.YesNow).success.value

          navigator.nextPage(AddAProtectorPage, fakeDraftId, answers)
            .mustBe(controllers.register.individual.routes.NameController.onPageLoad(0, fakeDraftId))
      }
    }

    "go to Business name page from AddAProtectorPage when selected add them now and 25 individual protectors" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = (0 until 25).foldLeft(userAnswers)((acc, index) => {
            acc
              .set(ind.NamePage(index), FullName("First", None, "Last")).success.value
              .set(IndividualProtectorStatus(index), Completed).success.value
          }).set(AddAProtectorPage, AddAProtector.YesNow).success.value

          navigator.nextPage(AddAProtectorPage, fakeDraftId, answers)
            .mustBe(controllers.register.business.routes.NameController.onPageLoad(0, fakeDraftId))
      }
    }
  }


  "go to RegistrationProgress from AddAProtectorPage" when {

    "selecting add them later" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers
            .set(bus.NamePage(0), "Business").success.value
            .set(AddAProtectorPage, AddAProtector.YesLater).success.value

          navigator.nextPage(AddAProtectorPage, fakeDraftId, answers)
            .mustBe(protectorsCompletedRoute(fakeDraftId, frontendAppConfig))
      }
    }

    "selecting added them all" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers
            .set(bus.NamePage(0), "Business").success.value
            .set(AddAProtectorPage, AddAProtector.NoComplete).success.value

          navigator.nextPage(AddAProtectorPage, fakeDraftId, answers)
            .mustBe(protectorsCompletedRoute(fakeDraftId, frontendAppConfig))
      }
    }

  }


  "IndividualOrBusinessPage" when {

    "Business option selected" must {

      val name = "Company"
      val selection = IndividualOrBusinessToAdd.Business

      "go to NamePage at index 0 when no other businesses" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(IndividualOrBusinessPage, selection).success.value

            navigator.nextPage(IndividualOrBusinessPage, fakeDraftId, answers)
              .mustBe(brts.NameController.onPageLoad(0, fakeDraftId))
        }
      }

      "go to NamePage at next index when there are businesses" in {
        forAll(arbitrary[UserAnswers], arbitrary[Status], Gen.choose(1, max)) {
          (userAnswers, status, n) =>

            val answers = (0 until n).foldLeft(userAnswers)((acc, index) => {
              val statusAtIndex = if (index == n - 1) Completed else status
              acc
                .set(bus.NamePage(index), name).success.value
                .set(BusinessProtectorStatus(index), statusAtIndex).success.value
            })

            val answersWithSelection = answers.set(IndividualOrBusinessPage, selection).success.value

            navigator.nextPage(IndividualOrBusinessPage, fakeDraftId, answersWithSelection)
              .mustBe(brts.NameController.onPageLoad(n, fakeDraftId))
        }
      }
    }

    "Individual option selected" must {

      val name = FullName("Joe", None, "Bloggs")
      val selection = IndividualOrBusinessToAdd.Individual

      "go to NamePage at index 0 when no other individuals" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(IndividualOrBusinessPage, selection).success.value

            navigator.nextPage(IndividualOrBusinessPage, fakeDraftId, answers)
              .mustBe(irts.NameController.onPageLoad(0, fakeDraftId))
        }
      }

      "go to NamePage at next index when there are individuals" in {
        forAll(arbitrary[UserAnswers], arbitrary[Status], Gen.choose(1, max)) {
          (userAnswers, status, n) =>

            val answers = (0 until n).foldLeft(userAnswers)((acc, index) => {
              val statusAtIndex = if (index == n - 1) Completed else status
              acc
                .set(ind.NamePage(index), name).success.value
                .set(IndividualProtectorStatus(index), statusAtIndex).success.value
            })

            val answersWithSelection = answers.set(IndividualOrBusinessPage, selection).success.value

            navigator.nextPage(IndividualOrBusinessPage, fakeDraftId, answersWithSelection)
              .mustBe(irts.NameController.onPageLoad(n, fakeDraftId))
        }
      }
    }
  }

  "TrustHasProtectorYesNoPage" when {

    "go to InfoPage from TrustHasProtectorYesNoPage when yes selected" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val answers = userAnswers.set(TrustHasProtectorYesNoPage, value = true).success.value

          navigator.nextPage(TrustHasProtectorYesNoPage, fakeDraftId, answers)
            .mustBe(rts.InfoController.onPageLoad(fakeDraftId))
      }
    }

    "go to RegistrationProgress from TrustHasProtectorYesNoPage when no selected" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val answers = userAnswers.set(TrustHasProtectorYesNoPage, value = false).success.value

          navigator.nextPage(TrustHasProtectorYesNoPage, fakeDraftId, answers)
            .mustBe(protectorsCompletedRoute(fakeDraftId, frontendAppConfig))
      }
    }

  }

}
