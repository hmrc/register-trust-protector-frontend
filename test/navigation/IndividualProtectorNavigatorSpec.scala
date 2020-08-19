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
import controllers.register.individual.{routes => rts}
import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.register.individual._

class IndividualProtectorNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new IndividualProtectorNavigator()
  val index = 0

  "Individual protector navigator" must {

    "NamePage -> DateOfBirthYesNoPage" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(NamePage(index), draftId, userAnswers)
            .mustBe(rts.DateOfBirthYesNoController.onPageLoad(index, draftId))
      }
    }

    "DateOfBirthYesNoPage -> Yes -> DateOfBirthPage" in {
      forAll(arbitrary[UserAnswers]) {
        baseAnswers =>
          val answers = baseAnswers.set(DateOfBirthYesNoPage(index), true).success.value
          navigator.nextPage(DateOfBirthYesNoPage(index), draftId, answers)
            .mustBe(rts.DateOfBirthController.onPageLoad(index, draftId))
      }
    }

    "DateOfBirthYesNoPage -> No -> NationalInsuranceYesNoPage" in {
      forAll(arbitrary[UserAnswers]) {
        baseAnswers =>
          val answers = baseAnswers.set(DateOfBirthYesNoPage(index), false).success.value
          navigator.nextPage(DateOfBirthYesNoPage(index), draftId, answers)
            .mustBe(rts.NationalInsuranceYesNoController.onPageLoad(index, draftId))
      }
    }

    "DateOfBirthPage -> NationalInsuranceYesNoPage" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(DateOfBirthPage(index), draftId, userAnswers)
            .mustBe(rts.NationalInsuranceYesNoController.onPageLoad(index, draftId))
      }
    }

    "NationalInsuranceYesNoPage -> Yes -> NationalInsurancePage" in {
      forAll(arbitrary[UserAnswers]) {
        baseAnswers =>
          val answers = baseAnswers.set(NationalInsuranceYesNoPage(index), true).success.value
          navigator.nextPage(NationalInsuranceYesNoPage(index), draftId, answers)
            .mustBe(rts.NationalInsuranceNumberController.onPageLoad(index, draftId))
      }
    }

    "NationalInsuranceYesNoPage -> No -> AddressYesNoPage" in {
      forAll(arbitrary[UserAnswers]) {
        baseAnswers =>
          val answers = baseAnswers.set(NationalInsuranceYesNoPage(index), false).success.value
          navigator.nextPage(NationalInsuranceYesNoPage(index), draftId, answers)
            .mustBe(rts.AddressYesNoController.onPageLoad(index, draftId))
      }
    }

    "NationalInsurancePage -> ???" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(NationalInsuranceNumberPage(index), draftId, userAnswers)
            .mustBe(rts.NationalInsuranceNumberController.onPageLoad(index, draftId)) // TODO
      }
    }
    
    "AddressYesNoPage -> Yes -> AddressUkYesNoPage" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage(index), true).success.value

      navigator.nextPage(AddressYesNoPage(index), draftId, answers)
        .mustBe(rts.AddressUkYesNoController.onPageLoad(index, draftId))
    }

    "AddressYesNoPage -> No -> ???" in {
      val answers = emptyUserAnswers
        .set(AddressYesNoPage(index), false).success.value

      navigator.nextPage(AddressYesNoPage(index), draftId, answers)
        .mustBe(rts.AddressYesNoController.onPageLoad(index, draftId)) // TODO
    }

    "AddressUkYesNoPage -> Yes -> UKAddressPage" in {
      val answers = emptyUserAnswers
        .set(AddressUkYesNoPage(index), true).success.value

      navigator.nextPage(AddressUkYesNoPage(index), draftId, answers)
        .mustBe(rts.UkAddressController.onPageLoad(index, draftId))
    }

    "AddressUkYesNoPage -> No -> NonUKAddressPage" in {
      val answers = emptyUserAnswers
        .set(AddressUkYesNoPage(index), false).success.value

      navigator.nextPage(AddressUkYesNoPage(index), draftId, answers)
        .mustBe(rts.NonUkAddressController.onPageLoad(index, draftId))
    }

    "UKAddressPage -> PassportDetailsYesNoController" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(UkAddressPage(index), draftId, userAnswers)
            .mustBe(rts.PassportDetailsYesNoController.onPageLoad(index, draftId))  // TODO
      }
    }

    "NonUKAddressPage -> PassportDetailsYesNoController" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(NonUkAddressPage(index), draftId, userAnswers)
            .mustBe(rts.PassportDetailsYesNoController.onPageLoad(index, draftId)) // TODO
      }
    }

    "PassportDetailsYesNoPage -> Yes -> PassportDetailsPage" in {
      val answers = emptyUserAnswers
        .set(PassportDetailsYesNoPage(index), true).success.value

      navigator.nextPage(PassportDetailsYesNoPage(index), draftId, answers)
        .mustBe(rts.PassportDetailsController.onPageLoad(index, draftId))
    }

    "PassportDetailsYesNoPage -> No -> IDCardDetailsYesNoPage" in {
      val answers = emptyUserAnswers
        .set(PassportDetailsYesNoPage(index), false).success.value

      navigator.nextPage(PassportDetailsYesNoPage(index), draftId, answers)
        .mustBe(rts.IDCardDetailsYesNoController.onPageLoad(index, draftId))
    }

    "IDCardDetailsYesNoPage -> Yes -> IDCardDetailsPage" in {
      val answers = emptyUserAnswers
        .set(IDCardDetailsYesNoPage(index), true).success.value

      navigator.nextPage(IDCardDetailsYesNoPage(index), draftId, answers)
        .mustBe(rts.IDCardDetailsController.onPageLoad(index, draftId))
    }

    "IDCardDetailsYesNoPage -> No -> ???" in {
      val answers = emptyUserAnswers
        .set(IDCardDetailsYesNoPage(index), false).success.value

      navigator.nextPage(IDCardDetailsYesNoPage(index), draftId, answers)
        .mustBe(rts.IDCardDetailsYesNoController.onPageLoad(index, draftId)) // TODO
    }

  }
}
