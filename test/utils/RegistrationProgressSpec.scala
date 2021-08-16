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

package utils

import base.SpecBase
import generators.ModelGenerators
import models.FullName
import models.Status._
import models.register.pages.AddAProtector
import models.register.pages.AddAProtector.NoComplete
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.entitystatus.{BusinessProtectorStatus, IndividualProtectorStatus}
import pages.register.{AddAProtectorPage, TrustHasProtectorYesNoPage, business => bus, individual => ind}

class RegistrationProgressSpec extends SpecBase with ScalaCheckPropertyChecks with ModelGenerators {

  private val registrationProgress: RegistrationProgress = injector.instanceOf[RegistrationProgress]

  private val business = "Amazon"
  private val individual = FullName("Joe", None, "Bloggs")

  "RegistrationProgress" must {

    "return None" when {

      "no protectors added and TrustHasProtectorYesNoPage is undefined" in {

        val userAnswers = emptyUserAnswers
        val result = registrationProgress.protectorsStatus(userAnswers)
        result mustBe None
      }
    }

    "return Some(Completed)" when {

      "no protectors added and TrustHasProtectorYesNoPage is false" in {

        val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, false).success.value
        val result = registrationProgress.protectorsStatus(userAnswers)
        result mustBe Some(Completed)
      }

      "protectors added, all are complete and NoComplete selected" in {

        val userAnswers = emptyUserAnswers
          .set(TrustHasProtectorYesNoPage, true).success.value
          .set(AddAProtectorPage, NoComplete).success.value

          .set(bus.NamePage(0), business).success.value
          .set(BusinessProtectorStatus(0), Completed).success.value
          .set(ind.NamePage(0), individual).success.value
          .set(IndividualProtectorStatus(0), Completed).success.value

        val result = registrationProgress.protectorsStatus(userAnswers)
        result mustBe Some(Completed)
      }
    }

    "return Some(InProgress)" when {

      "no protectors added and TrustHasProtectorYesNoPage is true" in {

        val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, true).success.value
        val result = registrationProgress.protectorsStatus(userAnswers)
        result mustBe Some(InProgress)
      }

      "protectors added but none are complete" in {

        val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, true).success.value
          .set(bus.NamePage(0), business).success.value
          .set(ind.NamePage(0), individual).success.value

        val result = registrationProgress.protectorsStatus(userAnswers)
        result mustBe Some(InProgress)
      }

      "protectors added but one business is in progress" in {

        val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, true).success.value
          .set(bus.NamePage(0), business).success.value
          .set(ind.NamePage(0), individual).success.value
          .set(IndividualProtectorStatus(0), Completed).success.value

        val result = registrationProgress.protectorsStatus(userAnswers)
        result mustBe Some(InProgress)
      }

      "protectors added but one individual is in progress" in {

        val userAnswers = emptyUserAnswers.set(TrustHasProtectorYesNoPage, true).success.value
          .set(bus.NamePage(0), business).success.value
          .set(BusinessProtectorStatus(0), Completed).success.value
          .set(ind.NamePage(0), individual).success.value

        val result = registrationProgress.protectorsStatus(userAnswers)
        result mustBe Some(InProgress)
      }

      "protectors added, all are complete but NoComplete not selected" in {

        forAll(arbitrary[AddAProtector].suchThat(_ != NoComplete)) { selection =>

          val userAnswers = emptyUserAnswers
            .set(TrustHasProtectorYesNoPage, true).success.value
            .set(AddAProtectorPage, selection).success.value

            .set(bus.NamePage(0), business).success.value
            .set(BusinessProtectorStatus(0), Completed).success.value
            .set(ind.NamePage(0), individual).success.value
            .set(IndividualProtectorStatus(0), Completed).success.value

          val result = registrationProgress.protectorsStatus(userAnswers)
          result mustBe Some(InProgress)
        }
      }
    }
  }

}
