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

package pages.individual.mld5

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.register.individual.mld5.{NationalityUkYesNoPage, NationalityPage, NationalityYesNoPage}

class NationalityYesNoPageSpec extends PageBehaviours {

  "NationalityYesNoPage" must {

    beRetrievable[Boolean](NationalityYesNoPage(0))

    beSettable[Boolean](NationalityYesNoPage(0))

    beRemovable[Boolean](NationalityYesNoPage(0))
  }

  "remove pages when CountryOfResidenceYesNoPage is set to false" in {
    forAll(arbitrary[UserAnswers]) {
      initial =>
        val answers: UserAnswers = initial.set(NationalityUkYesNoPage(0), false).success.value
          .set(NationalityPage(0), "ES").success.value

        val result = answers.set(NationalityYesNoPage(0), false).success.value

        result.get(NationalityUkYesNoPage(0)) mustNot be(defined)
        result.get(NationalityPage(0)) mustNot be(defined)
    }
  }
}