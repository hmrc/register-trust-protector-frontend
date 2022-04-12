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

package pages

import models.Status._
import models.register.pages.IndividualOrBusinessToAdd
import models.register.pages.IndividualOrBusinessToAdd._
import models.{FullName, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.entitystatus.{BusinessProtectorStatus, IndividualProtectorStatus}
import pages.register.{IndividualOrBusinessPage, business => bus, individual => ind}
import sections.{BusinessProtectors, IndividualProtectors}

class IndividualOrBusinessPageSpec extends PageBehaviours {

  val busName: String = "Business"
  val indName: FullName = FullName("Joe", None, "Bloggs")

  "IndividualOrBusinessPage" must {

    beRetrievable[IndividualOrBusinessToAdd](IndividualOrBusinessPage)

    beSettable[IndividualOrBusinessToAdd](IndividualOrBusinessPage)

    beRemovable[IndividualOrBusinessToAdd](IndividualOrBusinessPage)

    "implement cleanup" when {

      "individual selected" when {
        "last business is in progress" in {
          forAll(arbitrary[UserAnswers]) {
            initial =>
              val answers = initial
                .set(bus.NamePage(0), busName).success.value
                .set(BusinessProtectorStatus(0), InProgress).success.value

              val result = answers.set(IndividualOrBusinessPage, Individual).success.value

              result.get(BusinessProtectors).getOrElse(Nil).size mustBe 0
          }
        }
      }

      "business selected" when {
        "last individual is in progress" in {
          forAll(arbitrary[UserAnswers]) {
            initial =>
              val answers = initial
                .set(ind.NamePage(0), indName).success.value
                .set(IndividualProtectorStatus(0), InProgress).success.value

              val result = answers.set(IndividualOrBusinessPage, Business).success.value

              result.get(IndividualProtectors).getOrElse(Nil).size mustBe 0
          }
        }
      }
    }

    "not implement cleanup" when {

      "individual selected" when {

        "no businesses" in {
          forAll(arbitrary[UserAnswers]) {
            initial =>
              val result = initial.set(IndividualOrBusinessPage, Individual).success.value

              result.get(BusinessProtectors).getOrElse(Nil).size mustBe 0
          }
        }

        "last business is complete" in {
          forAll(arbitrary[UserAnswers]) {
            initial =>
              val answers = initial
                .set(bus.NamePage(0), busName).success.value
                .set(BusinessProtectorStatus(0), Completed).success.value

              val result = answers.set(IndividualOrBusinessPage, Individual).success.value

              result.get(BusinessProtectors).getOrElse(Nil).size mustBe 1
          }
        }
      }

      "business selected" when {

        "no individuals" in {
          forAll(arbitrary[UserAnswers]) {
            initial =>
              val result = initial.set(IndividualOrBusinessPage, Business).success.value

              result.get(IndividualProtectors).getOrElse(Nil).size mustBe 0
          }
        }

        "last individual is complete" in {
          forAll(arbitrary[UserAnswers]) {
            initial =>
              val answers = initial
                .set(ind.NamePage(0), indName).success.value
                .set(IndividualProtectorStatus(0), Completed).success.value

              val result = answers.set(IndividualOrBusinessPage, Business).success.value

              result.get(IndividualProtectors).getOrElse(Nil).size mustBe 1
          }
        }
      }
    }
  }
}
