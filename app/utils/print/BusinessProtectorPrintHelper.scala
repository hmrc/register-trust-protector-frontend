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

package utils.print

import com.google.inject.Inject
import controllers.register.business.mld5.{routes => mld5brts}
import controllers.register.business.{routes => brts}
import models.UserAnswers
import pages.register.business._
import pages.register.business.mld5.{CountryOfResidenceInTheUkYesNoPage, CountryOfResidencePage, CountryOfResidenceYesNoPage}
import play.api.i18n.Messages
import viewmodels.AnswerRow

class BusinessProtectorPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) extends PrintHelper {

  override val protectorType: String = "businessProtector"

  override def answers(userAnswers: UserAnswers, name: String, index: Int, draftId: String)
                      (implicit messages: Messages): Seq[AnswerRow] = {
    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers, name)

    Seq(
      bound.stringQuestion(NamePage(index), "businessProtector.name", brts.NameController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(UtrYesNoPage(index), "businessProtector.utrYesNo", brts.UtrYesNoController.onPageLoad(index, draftId).url),
      bound.stringQuestion(UtrPage(index), "businessProtector.utr", brts.UtrController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(CountryOfResidenceYesNoPage(index), "businessProtector.5mld.countryOfResidenceYesNo", mld5brts.CountryOfResidenceYesNoController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(CountryOfResidenceInTheUkYesNoPage(index), "businessProtector.5mld.countryOfResidenceInTheUkYesNo", mld5brts.CountryOfResidenceInTheUkYesNoController.onPageLoad(index, draftId).url),
      bound.countryQuestion(CountryOfResidenceInTheUkYesNoPage(index), CountryOfResidencePage(index), "businessProtector.5mld.countryOfResidence", mld5brts.CountryOfResidenceController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(AddressYesNoPage(index), "businessProtector.addressYesNo", brts.AddressYesNoController.onPageLoad(index, draftId).url),
      bound.yesNoQuestion(AddressUkYesNoPage(index), "businessProtector.addressUkYesNo", brts.AddressUkYesNoController.onPageLoad(index, draftId).url),
      bound.addressQuestion(UkAddressPage(index), "site.address.uk", brts.UkAddressController.onPageLoad(index, draftId).url),
      bound.addressQuestion(NonUkAddressPage(index), "site.address.international", brts.NonUkAddressController.onPageLoad(index, draftId).url)
    ).flatten
  }
}
