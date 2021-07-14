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

import base.SpecBase
import controllers.register.individual.mld5.routes._
import controllers.register.individual.routes._
import models.{FullName, InternationalAddress, PassportOrIdCardDetails, UkAddress}
import pages.register.individual._
import pages.register.individual.mld5._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class IndividualProtectorPrintHelperSpec extends SpecBase {

  private val index: Int = 0
  private val name: FullName = FullName("Joe", None, "Bloggs")
  private val arg = name.toString
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val nino: String = "AA000000A"
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "AB11AB")
  private val country: String = "FR"
  private val nonUkAddress: InternationalAddress = InternationalAddress("Line 1", "Line 2", Some("Line 3"), country)
  private val passportOrIdCard: PassportOrIdCardDetails = PassportOrIdCardDetails(country, "12345", date)
  private val canEdit: Boolean = true

  private val helper: IndividualProtectorPrintHelper = injector.instanceOf[IndividualProtectorPrintHelper]

  "Individual protector print helper" must {

    "return an individual protector answer section" in {

      val userAnswers = emptyUserAnswers
        .set(NamePage(index), name).success.value
        .set(DateOfBirthYesNoPage(index), true).success.value
        .set(DateOfBirthPage(index), date).success.value
        .set(NationalityYesNoPage(index), true).success.value
        .set(NationalityUkYesNoPage(index), false).success.value
        .set(NationalityPage(index), country).success.value
        .set(NationalInsuranceYesNoPage(index), true).success.value
        .set(NationalInsuranceNumberPage(index), nino).success.value
        .set(CountryOfResidenceYesNoPage(index), true).success.value
        .set(CountryOfResidenceInTheUkYesNoPage(index), false).success.value
        .set(CountryOfResidencePage(index), country).success.value
        .set(AddressYesNoPage(index), true).success.value
        .set(AddressUkYesNoPage(index), true).success.value
        .set(UkAddressPage(index), ukAddress).success.value
        .set(NonUkAddressPage(index), nonUkAddress).success.value
        .set(PassportDetailsYesNoPage(index), true).success.value
        .set(PassportDetailsPage(index), passportOrIdCard).success.value
        .set(IDCardDetailsYesNoPage(index), true).success.value
        .set(IDCardDetailsPage(index), passportOrIdCard).success.value
        .set(LegallyCapableYesNoPage(index), true).success.value

      val result = helper.printSection(userAnswers, Some(name.toString), index, fakeDraftId)

      result mustEqual AnswerSection(
        headingKey = Some("answerPage.section.individualProtector.subheading"),
        rows = Seq(
          AnswerRow("individualProtector.name.checkYourAnswersLabel", Html(name.displayFullName), Some(NameController.onPageLoad(index, fakeDraftId).url), "", canEdit),
          AnswerRow("individualProtector.dateOfBirthYesNo.checkYourAnswersLabel", Html("Yes"), Some(DateOfBirthYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.dateOfBirth.checkYourAnswersLabel", Html("3 February 1996"), Some(DateOfBirthController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.5mld.nationalityYesNo.checkYourAnswersLabel", Html("Yes"), Some(NationalityYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.5mld.nationalityUkYesNo.checkYourAnswersLabel", Html("No"), Some(NationalityUkYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.5mld.nationality.checkYourAnswersLabel", Html("France"), Some(NationalityController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.nationalInsuranceYesNo.checkYourAnswersLabel", Html("Yes"), Some(NationalInsuranceYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.nationalInsuranceNumber.checkYourAnswersLabel", Html("AA 00 00 00 A"), Some(NationalInsuranceNumberController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.5mld.countryOfResidenceYesNo.checkYourAnswersLabel", Html("Yes"), Some(CountryOfResidenceYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.5mld.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel", Html("No"), Some(CountryOfResidenceInTheUkYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.5mld.countryOfResidence.checkYourAnswersLabel", Html("France"), Some(CountryOfResidenceController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.addressYesNo.checkYourAnswersLabel", Html("Yes"), Some(AddressYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.addressUkYesNo.checkYourAnswersLabel", Html("Yes"), Some(AddressUkYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("site.address.uk.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />Line 3<br />Line 4<br />AB11AB"), Some(UkAddressController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("site.address.international.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />Line 3<br />France"), Some(NonUkAddressController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.passportDetailsYesNo.checkYourAnswersLabel", Html("Yes"), Some(PassportDetailsYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.passportDetails.checkYourAnswersLabel", Html("France<br />12345<br />3 February 1996"), Some(PassportDetailsController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.idCardDetailsYesNo.checkYourAnswersLabel", Html("Yes"), Some(IDCardDetailsYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.idCardDetails.checkYourAnswersLabel", Html("France<br />12345<br />3 February 1996"), Some(IDCardDetailsController.onPageLoad(index, fakeDraftId).url), arg, canEdit),
          AnswerRow("individualProtector.5mld.legallyCapableYesNo.checkYourAnswersLabel", Html("Yes"), Some(LegallyCapableYesNoController.onPageLoad(index, fakeDraftId).url), arg, canEdit)
        ),
        headingArgs = Seq(1)
      )
    }
  }
}
