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

package pages.business

import pages.behaviours.PageBehaviours
import pages.register.business.AddressUkYesNoPage


class AddressUkYesNoPageSpec extends PageBehaviours {

  "AddressUkYesNoPage" must {

    beRetrievable[Boolean](AddressUkYesNoPage(0))

    beSettable[Boolean](AddressUkYesNoPage(0))

    beRemovable[Boolean](AddressUkYesNoPage(0))

    // TODO
//    "implement cleanup logic when NO selected" in {
//      val userAnswers = UserAnswers("id", "utr", LocalDate.now)
//        .set(AddressUkYesNoPage, true)
//        .flatMap(_.set(UkAddressPage, UkAddress("line1", "line2", None, None, "postcode")))
//        .flatMap(_.set(AddressYesNoPage, false))
//
//      userAnswers.get.get(AddressUkYesNoPage) mustNot be(defined)
//      userAnswers.get.get(UkAddressPage) mustNot be(defined)
//    }
  }
}
