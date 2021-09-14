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

package utils.print

import com.google.inject.Inject
import controllers.register.individual.mld5.routes._
import controllers.register.individual.routes._
import models.UserAnswers
import pages.register.individual._
import pages.register.individual.mld5._
import play.api.i18n.Messages
import viewmodels.AnswerRow

class IndividualProtectorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) extends PrintHelper {

  override val protectorType: String = "individualProtector"

  override def answers(userAnswers: UserAnswers, name: String, index: Int, draftId: String)
                      (implicit messages: Messages): Seq[AnswerRow] = {
    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers, name)

    Seq(
      bound.nameQuestion(NamePage(index), "individualProtector.name", NameController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(DateOfBirthYesNoPage(index), "individualProtector.dateOfBirthYesNo", DateOfBirthYesNoController.onPageLoad(index, draftId).url),
      bound.dateQuestion(DateOfBirthPage(index), "individualProtector.dateOfBirth", DateOfBirthController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(NationalityYesNoPage(index), "individualProtector.5mld.nationalityYesNo", NationalityYesNoController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(NationalityUkYesNoPage(index), "individualProtector.5mld.nationalityUkYesNo", NationalityUkYesNoController.onPageLoad(index, draftId).url),
      bound.countryQuestion(NationalityUkYesNoPage(index), NationalityPage(index), "individualProtector.5mld.nationality", NationalityController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(NationalInsuranceYesNoPage(index), "individualProtector.nationalInsuranceYesNo", NationalInsuranceYesNoController.onPageLoad(index, draftId).url),
      bound.ninoQuestion(NationalInsuranceNumberPage(index), "individualProtector.nationalInsuranceNumber", NationalInsuranceNumberController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(CountryOfResidenceYesNoPage(index), "individualProtector.5mld.countryOfResidenceYesNo", CountryOfResidenceYesNoController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(CountryOfResidenceInTheUkYesNoPage(index), "individualProtector.5mld.countryOfResidenceInTheUkYesNo", CountryOfResidenceInTheUkYesNoController.onPageLoad(index, draftId).url),
      bound.countryQuestion(CountryOfResidenceInTheUkYesNoPage(index), CountryOfResidencePage(index), "individualProtector.5mld.countryOfResidence", CountryOfResidenceController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(AddressYesNoPage(index), "individualProtector.addressYesNo", AddressYesNoController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(AddressUkYesNoPage(index), "individualProtector.addressUkYesNo", AddressUkYesNoController.onPageLoad(index, draftId).url),
      bound.addressQuestion(UkAddressPage(index), "site.address.uk", UkAddressController.onPageLoad(index, draftId).url),
      bound.addressQuestion(NonUkAddressPage(index), "site.address.international", NonUkAddressController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(PassportDetailsYesNoPage(index), "individualProtector.passportDetailsYesNo", PassportDetailsYesNoController.onPageLoad(index, draftId).url),
      bound.passportDetailsQuestion(PassportDetailsPage(index), "individualProtector.passportDetails", PassportDetailsController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(IDCardDetailsYesNoPage(index), "individualProtector.idCardDetailsYesNo", IDCardDetailsYesNoController.onPageLoad(index, draftId).url),
      bound.passportDetailsQuestion(IDCardDetailsPage(index), "individualProtector.idCardDetails", IDCardDetailsController.onPageLoad(index, draftId).url),
      bound.enumQuestion(LegallyCapableYesNoPage(index), "individualProtector.5mld.legallyCapableYesNo", LegallyCapableYesNoController.onPageLoad(index, draftId).url, "site")
    ).flatten
  }
}
