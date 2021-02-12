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

import controllers.register.individual.mld5.{routes => mld5}
import controllers.register.individual.{routes => irts}
import controllers.register.{routes => rts}
import models.ReadableUserAnswers
import pages.Page
import pages.register.individual._
import pages.register.individual.mld5._
import play.api.mvc.Call

import javax.inject.Inject

class IndividualProtectorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call =
    routes(draftId)(page)(userAnswers)

  private def simpleNavigation(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case NamePage(index) => _ => irts.DateOfBirthYesNoController.onPageLoad(index, draftId)
    case DateOfBirthPage(index) => ua => navigateAwayFromDateOfBirthQuestions(index, draftId, ua.is5mldEnabled)
    case NationalityPage(index) => ua => navigateAwayFromNationalityQuestions(index, draftId, ua.isTaxable)
    case NationalInsuranceNumberPage(index) => ua => navigateAwayFromNinoQuestion(index, draftId, ua.is5mldEnabled)
    case CountryOfResidencePage(index) => ua => navigateAwayFromCountryOfResidencyQuestions(index, draftId, ua)
    case UkAddressPage(index) => _ => irts.PassportDetailsYesNoController.onPageLoad(index, draftId)
    case NonUkAddressPage(index) => _ => irts.PassportDetailsYesNoController.onPageLoad(index, draftId)
    case PassportDetailsPage(index) => ua => navigateAwayFromPassportOrIdQuestions(index, draftId, ua.is5mldEnabled)
    case IDCardDetailsPage(index) => ua =>navigateAwayFromPassportOrIdQuestions(index, draftId, ua.is5mldEnabled)
    case LegallyCapableYesNoPage(index) => _ => irts.CheckDetailsController.onPageLoad(index, draftId)
    case CheckDetailsPage => _ => rts.AddAProtectorController.onPageLoad(draftId)
  }

  private def yesNoNavigation(draftId: String) : PartialFunction[Page, ReadableUserAnswers => Call] = {
    case DateOfBirthYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        DateOfBirthYesNoPage(index),
        irts.DateOfBirthController.onPageLoad(index, draftId),
        navigateAwayFromDateOfBirthQuestions(index, draftId, ua.is5mldEnabled)
      )
    case NationalInsuranceYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        NationalInsuranceYesNoPage(index),
        irts.NationalInsuranceNumberController.onPageLoad(index, draftId),
        navigateAwayFromNinoYesNoQuestion(index, draftId, ua.is5mldEnabled)
      )
    case AddressYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        AddressYesNoPage(index),
        irts.AddressUkYesNoController.onPageLoad(index, draftId),
        navigateAwayFromPassportOrIdQuestions(index, draftId, ua.is5mldEnabled)
      )
    case CountryOfResidencePage(index) => ua =>
      yesNoNav(
        ua,
        NationalInsuranceYesNoPage(index),
        navigateAwayFromPassportOrIdQuestions(index, draftId, ua.is5mldEnabled),
        irts.AddressYesNoController.onPageLoad(index, draftId)
      )
    case AddressUkYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        AddressUkYesNoPage(index),
        irts.UkAddressController.onPageLoad(index, draftId),
        irts.NonUkAddressController.onPageLoad(index, draftId)
      )
    case PassportDetailsYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        PassportDetailsYesNoPage(index),
        irts.PassportDetailsController.onPageLoad(index, draftId),
        irts.IDCardDetailsYesNoController.onPageLoad(index, draftId)
      )
    case IDCardDetailsYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        IDCardDetailsYesNoPage(index),
        irts.IDCardDetailsController.onPageLoad(index, draftId),
        navigateAwayFromPassportOrIdQuestions(index, draftId, ua.is5mldEnabled)
      )
    case NationalityYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        NationalityYesNoPage(index),
        mld5.NationalityUkYesNoController.onPageLoad(index, draftId),
        navigateAwayFromNationalityQuestions(index, draftId, ua.isTaxable)
      )
    case CountryOfResidenceYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        CountryOfResidenceYesNoPage(index),
        mld5.CountryOfResidenceInTheUkYesNoController.onPageLoad(index, draftId),
        navigateAwayFromCountryOfResidencyQuestions(index, draftId, ua)
        )
    case NationalityUkYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        NationalityUkYesNoPage(index),
        navigateAwayFromNationalityQuestions(index, draftId, ua.isTaxable),
        mld5.NationalityController.onPageLoad(index, draftId)
      )
    case CountryOfResidenceInTheUkYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        CountryOfResidenceInTheUkYesNoPage(index),
        navigateAwayFromCountryOfResidencyQuestions(index, draftId, ua),
        mld5.CountryOfResidenceController.onPageLoad(index, draftId)
      )
  }

  private def navigateAwayFromDateOfBirthQuestions(index: Int, draftId: String, is5mldEnabled: Boolean): Call = {
    if (is5mldEnabled) {
      mld5.NationalityYesNoController.onPageLoad(index, draftId)
    } else {
      irts.NationalInsuranceYesNoController.onPageLoad(index, draftId)
    }
  }

  private def navigateAwayFromNinoYesNoQuestion(index: Int, draftId: String, is5mldEnabled: Boolean): Call = {
    if(is5mldEnabled) {
      mld5.CountryOfResidenceYesNoController.onPageLoad(index, draftId)
    } else {
      irts.AddressYesNoController.onPageLoad(index, draftId)
    }
  }

  private def navigateAwayFromNinoQuestion(index: Int, draftId: String, is5mldEnabled: Boolean): Call = {
    if(is5mldEnabled) {
      mld5.CountryOfResidenceYesNoController.onPageLoad(index, draftId)
    } else {
      irts.CheckDetailsController.onPageLoad(index, draftId)
    }
  }

  private def navigateAwayFromPassportOrIdQuestions(index: Int, draftId: String, is5mldEnabled: Boolean): Call = {
    if(is5mldEnabled){
      mld5.LegallyCapableYesNoController.onPageLoad(index, draftId)
    } else {
      irts.CheckDetailsController.onPageLoad(index, draftId)
    }
  }

  private def navigateAwayFromNationalityQuestions(index: Int, draftId: String, isTaxable: Boolean): Call = {
    if (isTaxable) {
      irts.NationalInsuranceYesNoController.onPageLoad(index, draftId)
    } else {
      mld5.CountryOfResidenceYesNoController.onPageLoad(index, draftId)
    }
  }

  private def navigateAwayFromCountryOfResidencyQuestions(index: Int, draftId: String, answers: ReadableUserAnswers): Call = {
    answers.get(NationalInsuranceYesNoPage(index)) match {
      case Some(false) if answers.isTaxable =>
        irts.AddressYesNoController.onPageLoad(index, draftId)
      case _ =>
        mld5.LegallyCapableYesNoController.onPageLoad(index, draftId)
    }
  }
  private def routes(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    simpleNavigation(draftId) orElse yesNoNavigation(draftId)
  }

}

