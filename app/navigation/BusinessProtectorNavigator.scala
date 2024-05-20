/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.register.business.mld5.routes._
import controllers.register.business.routes._
import models.ReadableUserAnswers
import pages.Page
import pages.register.business.mld5.{CountryOfResidenceInTheUkYesNoPage, CountryOfResidencePage, CountryOfResidenceYesNoPage}
import pages.register.business._
import play.api.mvc.Call

import javax.inject.Inject

class BusinessProtectorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call =
    routes(draftId)(page)(userAnswers)

  private def simpleNavigation(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case NamePage(index) => ua =>
      if (ua.isTaxable) {
        UtrYesNoController.onPageLoad(index, draftId)
      } else {
        CountryOfResidenceYesNoController.onPageLoad(index, draftId)
      }
    case UtrPage(index) => ua => CountryOfResidenceYesNoController.onPageLoad(index, draftId)
    case UkAddressPage(index) => _ => CheckDetailsController.onPageLoad(index, draftId)
    case NonUkAddressPage(index) => _ => CheckDetailsController.onPageLoad(index, draftId)
    case CountryOfResidencePage(index) => ua => addressOrCheckAnswersRoute(draftId, index, ua)
  }

  private def yesNoNavigation(draftId: String) : PartialFunction[Page, ReadableUserAnswers => Call] = {
    case UtrYesNoPage(index) => ua =>
      yesNoNav(
        ua = ua,
        fromPage = UtrYesNoPage(index),
        yesCall = UtrController.onPageLoad(index, draftId),
        noCall = CountryOfResidenceYesNoController.onPageLoad(index, draftId)
      )
    case AddressYesNoPage(index) => ua =>
      yesNoNav(
        ua = ua,
        fromPage = AddressYesNoPage(index),
        yesCall = AddressUkYesNoController.onPageLoad(index, draftId),
        noCall = CheckDetailsController.onPageLoad(index, draftId)
      )
    case AddressUkYesNoPage(index) => ua =>
      yesNoNav(
        ua = ua,
        fromPage = AddressUkYesNoPage(index),
        yesCall = UkAddressController.onPageLoad(index, draftId),
        noCall = NonUkAddressController.onPageLoad(index, draftId)
      )
    case CountryOfResidenceYesNoPage(index) => ua =>
      yesNoNav(
        ua = ua,
        fromPage = CountryOfResidenceYesNoPage(index),
        yesCall = CountryOfResidenceInTheUkYesNoController.onPageLoad(index, draftId),
        noCall = addressOrCheckAnswersRoute(draftId, index, ua)
      )
    case CountryOfResidenceInTheUkYesNoPage(index) => ua =>
      yesNoNav(
        ua = ua,
        fromPage = CountryOfResidenceInTheUkYesNoPage(index),
        yesCall = addressOrCheckAnswersRoute(draftId, index, ua),
        noCall = CountryOfResidenceController.onPageLoad(index, draftId)
      )
  }

  private def routes(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    simpleNavigation(draftId) orElse
      yesNoNavigation(draftId)
  }

  private def addressOrCheckAnswersRoute(draftId: String, index: Int, userAnswers: ReadableUserAnswers): Call = {
    val isTaxable: Boolean = userAnswers.isTaxable
    val isUtrDefined: Boolean = userAnswers.get(UtrPage(index)).isDefined

    (isTaxable, isUtrDefined) match {
      case (true, false) =>
        AddressYesNoController.onPageLoad(index, draftId)
      case _ =>
        CheckDetailsController.onPageLoad(index, draftId)
    }
  }

}
