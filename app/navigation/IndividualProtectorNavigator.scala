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

import controllers.register.individual.{routes => rts}
import javax.inject.Inject
import models.ReadableUserAnswers
import pages.Page
import pages.register.individual._
import play.api.mvc.Call

class IndividualProtectorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call = routes(draftId)(page)(userAnswers)

  private def simpleNavigation(draftId: String): PartialFunction[Page, Call] = {
    case NamePage(index) => rts.DateOfBirthYesNoController.onPageLoad(index, draftId)
    case DateOfBirthPage(index) => rts.NationalInsuranceYesNoController.onPageLoad(index, draftId)
    case NationalInsuranceNumberPage(index) => rts.NationalInsuranceNumberController.onPageLoad(index, draftId) // TODO
    case UkAddressPage(index) => rts.PassportDetailsYesNoController.onPageLoad(index, draftId)
    case NonUkAddressPage(index) => rts.PassportDetailsYesNoController.onPageLoad(index, draftId)
    case PassportDetailsPage(index) => rts.PassportDetailsController.onPageLoad(index, draftId) // TODO
    case IDCardDetailsPage(index) => rts.IDCardDetailsController.onPageLoad(index, draftId) // TODO
  }

  private def yesNoNavigation(draftId: String) : PartialFunction[Page, ReadableUserAnswers => Call] = {
    case DateOfBirthYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        DateOfBirthYesNoPage(index),
        rts.DateOfBirthController.onPageLoad(index, draftId),
        rts.NationalInsuranceYesNoController.onPageLoad(index, draftId))
    case NationalInsuranceYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        NationalInsuranceYesNoPage(index),
        rts.NationalInsuranceNumberController.onPageLoad(index, draftId),
        rts.AddressYesNoController.onPageLoad(index, draftId))
    case AddressYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        AddressYesNoPage(index),
        rts.AddressUkYesNoController.onPageLoad(index, draftId),
        rts.AddressYesNoController.onPageLoad(index, draftId))  // TODO
    case AddressUkYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        AddressUkYesNoPage(index),
        rts.UkAddressController.onPageLoad(index, draftId),
        rts.NonUkAddressController.onPageLoad(index, draftId))
    case PassportDetailsYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        PassportDetailsYesNoPage(index),
        rts.PassportDetailsController.onPageLoad(index, draftId),
        rts.IDCardDetailsYesNoController.onPageLoad(index, draftId))
    case IDCardDetailsYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        IDCardDetailsYesNoPage(index),
        rts.IDCardDetailsController.onPageLoad(index, draftId),
        rts.IDCardDetailsYesNoController.onPageLoad(index, draftId)) // TODO
  }

  private def routes(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    simpleNavigation(draftId) andThen (c => (_:ReadableUserAnswers) => c) orElse
      yesNoNavigation(draftId)
  }

}

