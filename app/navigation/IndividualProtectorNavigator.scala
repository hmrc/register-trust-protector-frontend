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

import controllers.register.{routes => rts}
import controllers.register.individual.{routes => irts}
import controllers.register.individual.mld5.{routes => mld5}

import javax.inject.Inject
import models.ReadableUserAnswers
import pages.Page
import pages.register.individual._
import pages.register.individual.mld5._
import play.api.mvc.Call

class IndividualProtectorNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call =
    nextPage(page, draftId, false, userAnswers)

  override def nextPage(page: Page, draftId: String, is5mld: Boolean, userAnswers: ReadableUserAnswers): Call =
    routes(draftId, is5mld)(page)(userAnswers)

  private def simpleNavigation(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case NamePage(index) => _ => irts.DateOfBirthYesNoController.onPageLoad(index, draftId)
    case NationalityPage(index) => _ => irts.NationalInsuranceYesNoController.onPageLoad(index, draftId)
    case CountryOfResidencePage(index) => _ => irts.AddressYesNoController.onPageLoad(index, draftId)
    case UkAddressPage(index) => _ => irts.PassportDetailsYesNoController.onPageLoad(index, draftId)
    case NonUkAddressPage(index) => _ => irts.PassportDetailsYesNoController.onPageLoad(index, draftId)
    case LegallyCapableYesNoPage(index) => _ => irts.CheckDetailsController.onPageLoad(index, draftId)
    case CheckDetailsPage => _ => rts.AddAProtectorController.onPageLoad(draftId)
  }

  private def is5mldNav(draftId: String, is5mld: Boolean) : PartialFunction[Page, ReadableUserAnswers => Call] = {
    case DateOfBirthPage(index) => _ =>
      if(is5mld) {
        mld5.NationalityYesNoController.onPageLoad(index, draftId)
      } else {
        irts.NationalInsuranceYesNoController.onPageLoad(index, draftId)
      }
    case DateOfBirthYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        DateOfBirthYesNoPage(index),
        irts.DateOfBirthController.onPageLoad(index, draftId),
        if(is5mld) {
          mld5.NationalityYesNoController.onPageLoad(index, draftId)
        } else {
          irts.NationalInsuranceYesNoController.onPageLoad(index, draftId)
        }
      )
    case NationalInsuranceNumberPage(index) => _ =>
      if(is5mld) {
        controllers.routes.IndexController.onPageLoad(draftId)
      } else {
        irts.CheckDetailsController.onPageLoad(index, draftId)
      }
    case NationalInsuranceYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        NationalInsuranceYesNoPage(index),
        irts.NationalInsuranceNumberController.onPageLoad(index, draftId),
        if(is5mld) {
          mld5.CountryOfResidenceYesNoController.onPageLoad(index, draftId)
        } else {
          irts.AddressYesNoController.onPageLoad(index, draftId)
        }
      )
    case PassportDetailsPage(index) => _ =>
      if(is5mld){
        mld5.LegallyCapableYesNoController.onPageLoad(index, draftId)
      } else {
        irts.CheckDetailsController.onPageLoad(index, draftId)
      }
    case IDCardDetailsYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        IDCardDetailsYesNoPage(index),
        irts.IDCardDetailsController.onPageLoad(index, draftId),
        if(is5mld){
          mld5.LegallyCapableYesNoController.onPageLoad(index, draftId)
        } else {
          irts.CheckDetailsController.onPageLoad(index, draftId)
        })
    case IDCardDetailsPage(index) => _ =>
      if(is5mld){
        mld5.LegallyCapableYesNoController.onPageLoad(index, draftId)
      } else {
        irts.CheckDetailsController.onPageLoad(index, draftId)
      }
  }

  private def yesNoNavigation(draftId: String) : PartialFunction[Page, ReadableUserAnswers => Call] = {
    case AddressYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        AddressYesNoPage(index),
        irts.AddressUkYesNoController.onPageLoad(index, draftId),
        irts.CheckDetailsController.onPageLoad(index, draftId))
    case AddressUkYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        AddressUkYesNoPage(index),
        irts.UkAddressController.onPageLoad(index, draftId),
        irts.NonUkAddressController.onPageLoad(index, draftId))
    case PassportDetailsYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        PassportDetailsYesNoPage(index),
        irts.PassportDetailsController.onPageLoad(index, draftId),
        irts.IDCardDetailsYesNoController.onPageLoad(index, draftId))
    case NationalityYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        NationalityYesNoPage(index),
        mld5.NationalityUkYesNoController.onPageLoad(index, draftId),
        irts.NationalInsuranceYesNoController.onPageLoad(index, draftId))
    case CountryOfResidenceYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        CountryOfResidenceYesNoPage(index),
        mld5.CountryOfResidenceInTheUkYesNoController.onPageLoad(index, draftId),
        irts.AddressYesNoController.onPageLoad(index, draftId))
    case NationalityUkYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        NationalityUkYesNoPage(index),
        irts.NationalInsuranceYesNoController.onPageLoad(index, draftId),
        mld5.NationalityController.onPageLoad(index, draftId))
    case CountryOfResidenceInTheUkYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        CountryOfResidenceInTheUkYesNoPage(index),
        irts.AddressYesNoController.onPageLoad(index, draftId),
        mld5.CountryOfResidenceController.onPageLoad(index, draftId))
  }

  private def routes(draftId: String, is5mld: Boolean): PartialFunction[Page, ReadableUserAnswers => Call] = {
    simpleNavigation(draftId) orElse yesNoNavigation(draftId) orElse is5mldNav(draftId, is5mld)
  }

}

