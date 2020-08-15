/*
 * Copyright 2020 HM Revenue & Customs
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
import models._
import controllers.register.business.{routes => brts}
import generators.Generators
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.register.business.{NamePage, UtrYesNoPage}
import org.scalacheck.Arbitrary.arbitrary

class BusinessProtectorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new BusinessProtectorNavigator(frontendAppConfig)
  val index = 0

  "Business protector navigator" must {

    "go to DiscretionYesNo from NamePage" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(NamePage(index), fakeDraftId, userAnswers)
            .mustBe(brts.UtrYesNoController.onPageLoad(index, fakeDraftId))
      }
    }

    "go to UtrPage from UtrYesNoPage if Yes" in {
      forAll(arbitrary[UserAnswers]) {
        baseAnswers =>
          val answers = baseAnswers.set(UtrYesNoPage(index), true).success.value
          navigator.nextPage(UtrYesNoPage(index), fakeDraftId, answers)
            .mustBe(brts.UtrController.onPageLoad(index, fakeDraftId))
      }
    }

    "go to ??? from UtrYesNoPage if No" in {
      forAll(arbitrary[UserAnswers]) {
        baseAnswers =>
          val answers = baseAnswers.set(UtrYesNoPage(index), false).success.value
          navigator.nextPage(UtrYesNoPage(index), fakeDraftId, answers)
            .mustBe(brts.UtrYesNoController.onPageLoad(index, fakeDraftId))  // TODO
      }
    }

  }
}
