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

package pages.individual.mld5

import models.YesNoDontKnow
import pages.behaviours.PageBehaviours
import pages.register.individual.mld5.LegallyCapableYesNoPage

class LegallyCapableYesNoPageSpec extends PageBehaviours {

  "LegallyCapableYesNoPage" must {

    beRetrievable[YesNoDontKnow](LegallyCapableYesNoPage(0))

    beSettable[YesNoDontKnow](LegallyCapableYesNoPage(0))

    beRemovable[YesNoDontKnow](LegallyCapableYesNoPage(0))
  }

}