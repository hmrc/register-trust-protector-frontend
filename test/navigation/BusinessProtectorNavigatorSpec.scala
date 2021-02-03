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

package navigation

import base.SpecBase
import controllers.register.business.{routes => brts}
import controllers.register.business.mld5.{routes => mld5Routes}
import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.register.business._
import pages.register.business.mld5.{CountryOfResidenceInTheUkYesNoPage, CountryOfResidencePage, CountryOfResidenceYesNoPage}

class BusinessProtectorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new BusinessProtectorNavigator(frontendAppConfig)
  val index = 0

  "Business protector navigator" must {

    "a 4mld trust" must {

      "NamePage -> UtrYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(NamePage(index), fakeDraftId, userAnswers)
              .mustBe(brts.UtrYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "UtrYesNoPage -> Yes -> UtrPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>
            val answers = baseAnswers.set(UtrYesNoPage(index), true).success.value
            navigator.nextPage(UtrYesNoPage(index), fakeDraftId, answers)
              .mustBe(brts.UtrController.onPageLoad(index, fakeDraftId))
        }
      }

      "UtrYesNoPage -> No -> AddressYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>
            val answers = baseAnswers.set(UtrYesNoPage(index), false).success.value
            navigator.nextPage(UtrYesNoPage(index), fakeDraftId, answers)
              .mustBe(brts.AddressYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "UtrPage -> CheckDetailsPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(UtrPage(index), fakeDraftId, userAnswers)
              .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
        }
      }

      "AddressYesNoPage -> Yes -> AddressUkYesNoPage" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage(index), true).success.value

        navigator.nextPage(AddressYesNoPage(index), fakeDraftId, answers)
          .mustBe(brts.AddressUkYesNoController.onPageLoad(index, fakeDraftId))
      }

      "AddressYesNoPage -> No -> CheckDetailsPage" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage(index), false).success.value

        navigator.nextPage(AddressYesNoPage(index), fakeDraftId, answers)
          .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
      }

      "AddressUkYesNoPage -> Yes -> UKAddressPage" in {
        val answers = emptyUserAnswers
          .set(AddressUkYesNoPage(index), true).success.value

        navigator.nextPage(AddressUkYesNoPage(index), fakeDraftId, answers)
          .mustBe(brts.UkAddressController.onPageLoad(index, fakeDraftId))
      }

      "AddressUkYesNoPage -> No -> NonUKAddressPage" in {
        val answers = emptyUserAnswers
          .set(AddressUkYesNoPage(index), false).success.value

        navigator.nextPage(AddressUkYesNoPage(index), fakeDraftId, answers)
          .mustBe(brts.NonUkAddressController.onPageLoad(index, fakeDraftId))
      }

      "UKAddressPage -> CheckDetailsPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(UkAddressPage(index), fakeDraftId, userAnswers)
              .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
        }
      }

      "NonUKAddressPage -> CheckDetailsPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(NonUkAddressPage(index), fakeDraftId, userAnswers)
              .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
        }
      }
    }

    "a 5mld trust" must {

      "NamePage -> UtrYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          _ =>
            val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true)

            navigator.nextPage(NamePage(index), fakeDraftId, baseAnswers)
              .mustBe(brts.UtrYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "UtrYesNoPage -> Yes -> UtrPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>
            val answers = baseAnswers.set(UtrYesNoPage(index), true).success.value
            navigator.nextPage(UtrYesNoPage(index), fakeDraftId, answers)
              .mustBe(brts.UtrController.onPageLoad(index, fakeDraftId))
        }
      }

      "UtrYesNoPage -> No -> ResidencyYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>

            val mld5Answers = baseAnswers.copy(is5mldEnabled = true, isTaxable = true)

            val answers = mld5Answers.set(UtrYesNoPage(index), false).success.value
            navigator.nextPage(UtrYesNoPage(index), fakeDraftId, answers)
              .mustBe(mld5Routes.CountryOfResidenceYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "UtrPage -> ResidencyYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val mld5Answers = userAnswers.copy(is5mldEnabled = true, isTaxable = true)

            navigator.nextPage(UtrPage(index), fakeDraftId, mld5Answers)
              .mustBe(mld5Routes.CountryOfResidenceYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "ResidencyYesNoPage -> Yes -> ResidencyUkYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>

            val mld5Answers = baseAnswers.copy(is5mldEnabled = true, isTaxable = true)
              .set(CountryOfResidenceYesNoPage(index), true).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage(index), fakeDraftId, mld5Answers)
              .mustBe(mld5Routes.CountryOfResidenceInTheUkYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "ResidencyYesNoPage -> No (without UTR) -> AddressYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>

            val mld5Answers = baseAnswers.copy(is5mldEnabled = true, isTaxable = true)
              .set(UtrYesNoPage(index), false).success.value
              .set(CountryOfResidenceYesNoPage(index), false).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage(index), fakeDraftId, mld5Answers)
              .mustBe(brts.AddressYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "ResidencyYesNoPage -> No (with UTR) -> CheckDetailsPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>

            val mld5Answers = baseAnswers.copy(is5mldEnabled = true, isTaxable = true)
              .set(UtrYesNoPage(index), true).success.value
              .set(CountryOfResidenceYesNoPage(index), false).success.value

            navigator.nextPage(CountryOfResidenceYesNoPage(index), fakeDraftId, mld5Answers)
              .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
        }
      }

      "ResidencyUkYesNoPage -> Yes (without UTR) -> AddressYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>

            val mld5Answers = baseAnswers.copy(is5mldEnabled = true, isTaxable = true)
              .set(UtrYesNoPage(index), false).success.value
              .set(CountryOfResidenceInTheUkYesNoPage(index), true).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage(index), fakeDraftId, mld5Answers)
              .mustBe(brts.AddressYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "ResidencyUkYesNoPage -> Yes (with UTR) -> CheckDetailsPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>

            val mld5Answers = baseAnswers.copy(is5mldEnabled = true, isTaxable = true)
              .set(UtrYesNoPage(index), true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage(index), true).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage(index), fakeDraftId, mld5Answers)
              .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
        }
      }

      "ResidencyUkYesNoPage -> No -> CountryOfResidencePage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>

            val mld5Answers = baseAnswers.copy(is5mldEnabled = true, isTaxable = true)
              .set(CountryOfResidenceInTheUkYesNoPage(index), false).success.value

            navigator.nextPage(CountryOfResidenceInTheUkYesNoPage(index), fakeDraftId, mld5Answers)
              .mustBe(mld5Routes.CountryOfResidenceController.onPageLoad(index, fakeDraftId))
        }
      }

      "CountryOfResidencePage -> (with UTR) -> CheckDetailsPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>

            val mld5Answers = baseAnswers.copy(is5mldEnabled = true, isTaxable = true)
              .set(UtrYesNoPage(index), true).success.value
              .set(CountryOfResidencePage(index), "FR").success.value

            navigator.nextPage(CountryOfResidencePage(index), fakeDraftId, mld5Answers)
              .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
        }
      }

      "CountryOfResidencePage -> (without UTR) -> AddressYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          baseAnswers =>

            val mld5Answers = baseAnswers.copy(is5mldEnabled = true, isTaxable = true)
              .set(UtrYesNoPage(index), false).success.value
              .set(CountryOfResidencePage(index), "FR").success.value

            navigator.nextPage(CountryOfResidencePage(index), fakeDraftId, mld5Answers)
              .mustBe(brts.AddressYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "AddressYesNoPage -> Yes -> AddressUkYesNoPage" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage(index), true).success.value

        navigator.nextPage(AddressYesNoPage(index), fakeDraftId, answers)
          .mustBe(brts.AddressUkYesNoController.onPageLoad(index, fakeDraftId))
      }

      "AddressYesNoPage -> No -> CheckDetailsPage" in {
        val answers = emptyUserAnswers
          .set(AddressYesNoPage(index), false).success.value

        navigator.nextPage(AddressYesNoPage(index), fakeDraftId, answers)
          .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
      }

      "AddressUkYesNoPage -> Yes -> UKAddressPage" in {
        val answers = emptyUserAnswers
          .set(AddressUkYesNoPage(index), true).success.value

        navigator.nextPage(AddressUkYesNoPage(index), fakeDraftId, answers)
          .mustBe(brts.UkAddressController.onPageLoad(index, fakeDraftId))
      }

      "AddressUkYesNoPage -> No -> NonUKAddressPage" in {
        val answers = emptyUserAnswers
          .set(AddressUkYesNoPage(index), false).success.value

        navigator.nextPage(AddressUkYesNoPage(index), fakeDraftId, answers)
          .mustBe(brts.NonUkAddressController.onPageLoad(index, fakeDraftId))
      }

      "UKAddressPage -> CheckDetailsPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(UkAddressPage(index), fakeDraftId, userAnswers)
              .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
        }
      }

      "NonUKAddressPage -> CheckDetailsPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(NonUkAddressPage(index), fakeDraftId, userAnswers)
              .mustBe(brts.CheckDetailsController.onPageLoad(index, fakeDraftId))
        }
      }

    }

  }
}
