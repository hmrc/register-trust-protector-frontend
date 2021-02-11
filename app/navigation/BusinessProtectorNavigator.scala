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

import controllers.register.business.mld5.{routes => mld5brts}
import controllers.register.business.{routes => brts}
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
    case NamePage(index) => _ => brts.UtrYesNoController.onPageLoad(index, draftId)
    case UtrPage(index) => ua => navigateAwayFromUtrPage(draftId, index, ua)
    case UkAddressPage(index) => _ => brts.CheckDetailsController.onPageLoad(index, draftId)
    case NonUkAddressPage(index) => _ => brts.CheckDetailsController.onPageLoad(index, draftId)
    case CountryOfResidencePage(index) => ua => addressOrCheckAnswersRoute(draftId, index, ua)
  }

  private def yesNoNavigation(draftId: String) : PartialFunction[Page, ReadableUserAnswers => Call] = {
    case UtrYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        UtrYesNoPage(index),
        brts.UtrController.onPageLoad(index, draftId),
        navigateAwayFromUtrYesNoPage(draftId, index, ua)
      )
    case AddressYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        AddressYesNoPage(index),
        brts.AddressUkYesNoController.onPageLoad(index, draftId),
        brts.CheckDetailsController.onPageLoad(index, draftId)
      )
    case AddressUkYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        AddressUkYesNoPage(index),
        brts.UkAddressController.onPageLoad(index, draftId),
        brts.NonUkAddressController.onPageLoad(index, draftId)
      )
    case CountryOfResidenceYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        CountryOfResidenceYesNoPage(index),
        mld5brts.CountryOfResidenceInTheUkYesNoController.onPageLoad(index, draftId),
        addressOrCheckAnswersRoute(draftId, index, ua)
      )
    case CountryOfResidenceInTheUkYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        CountryOfResidenceInTheUkYesNoPage(index),
        addressOrCheckAnswersRoute(draftId, index, ua),
        mld5brts.CountryOfResidenceController.onPageLoad(index, draftId)
      )
  }

  private def routes(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    simpleNavigation(draftId) orElse
      yesNoNavigation(draftId)
  }

  private def addressOrCheckAnswersRoute(draftId: String, index: Int, userAnswers: ReadableUserAnswers): Call = {
    userAnswers.get(UtrYesNoPage(index)) match {
      case Some(true) =>
        brts.CheckDetailsController.onPageLoad(index, draftId)
      case Some(false) =>
        brts.AddressYesNoController.onPageLoad(index, draftId)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def navigateAwayFromUtrPage(draftId: String, index: Int, userAnswers: ReadableUserAnswers): Call = {
    if (userAnswers.is5mldEnabled) {
      mld5brts.CountryOfResidenceYesNoController.onPageLoad(index, draftId)
    } else {
      brts.CheckDetailsController.onPageLoad(index, draftId)
    }
  }

  private def navigateAwayFromUtrYesNoPage(draftId: String, index: Int, userAnswers: ReadableUserAnswers): Call = {
    if (userAnswers.is5mldEnabled) {
      mld5brts.CountryOfResidenceYesNoController.onPageLoad(index, draftId)
    } else {
      brts.AddressYesNoController.onPageLoad(index, draftId)
    }
  }

}

