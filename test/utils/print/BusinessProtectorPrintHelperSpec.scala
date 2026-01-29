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

import base.SpecBase
import models.{InternationalAddress, UkAddress}
import pages.register.business.mld5.{
  CountryOfResidenceInTheUkYesNoPage, CountryOfResidencePage, CountryOfResidenceYesNoPage
}
import pages.register.business._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class BusinessProtectorPrintHelperSpec extends SpecBase {

  private val helper: BusinessProtectorPrintHelper = injector.instanceOf[BusinessProtectorPrintHelper]
  private val index: Int                           = 0
  private val name: String                         = "Business Name"
  private val utr: String                          = "1234567890"

  "BusinessProtectorPrintHelper" must {

    "render a print section" when {

      "name and UTR" in {
        val userAnswers = emptyUserAnswers
          .set(NamePage(index), name)
          .success
          .value
          .set(UtrYesNoPage(index), true)
          .success
          .value
          .set(UtrPage(index), utr)
          .success
          .value

        val result = helper.printSection(userAnswers, Some(name), index, draftId)
        result mustBe AnswerSection(
          headingKey = Some("answerPage.section.businessProtector.subheading"),
          rows = Seq(
            AnswerRow(
              "businessProtector.name.checkYourAnswersLabel",
              Html("Business Name"),
              changeUrl = Some(controllers.register.business.routes.NameController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utrYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(controllers.register.business.routes.UtrYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utr.checkYourAnswersLabel",
              Html(utr),
              changeUrl = Some(controllers.register.business.routes.UtrController.onPageLoad(index, draftId).url),
              labelArg = name
            )
          ),
          headingArgs = Seq(1)
        )
      }

      "name, UTR and UK residency" in {
        val userAnswers = emptyUserAnswers
          .set(NamePage(index), name)
          .success
          .value
          .set(UtrYesNoPage(index), true)
          .success
          .value
          .set(UtrPage(index), utr)
          .success
          .value
          .set(CountryOfResidenceYesNoPage(index), true)
          .success
          .value
          .set(CountryOfResidenceInTheUkYesNoPage(index), true)
          .success
          .value

        val result = helper.printSection(userAnswers, Some(name), index, draftId)
        result mustBe AnswerSection(
          headingKey = Some("answerPage.section.businessProtector.subheading"),
          rows = Seq(
            AnswerRow(
              "businessProtector.name.checkYourAnswersLabel",
              Html("Business Name"),
              changeUrl = Some(controllers.register.business.routes.NameController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utrYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(controllers.register.business.routes.UtrYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utr.checkYourAnswersLabel",
              Html(utr),
              changeUrl = Some(controllers.register.business.routes.UtrController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceInTheUkYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            )
          ),
          headingArgs = Seq(1)
        )
      }

      "name, UTR and Non-UK residency" in {
        val userAnswers = emptyUserAnswers
          .set(NamePage(index), name)
          .success
          .value
          .set(UtrYesNoPage(index), true)
          .success
          .value
          .set(UtrPage(index), utr)
          .success
          .value
          .set(CountryOfResidenceYesNoPage(index), true)
          .success
          .value
          .set(CountryOfResidenceInTheUkYesNoPage(index), false)
          .success
          .value
          .set(CountryOfResidencePage(index), "FR")
          .success
          .value

        val result = helper.printSection(userAnswers, Some(name), index, draftId)
        result mustBe AnswerSection(
          headingKey = Some("answerPage.section.businessProtector.subheading"),
          rows = Seq(
            AnswerRow(
              "businessProtector.name.checkYourAnswersLabel",
              Html("Business Name"),
              changeUrl = Some(controllers.register.business.routes.NameController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utrYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(controllers.register.business.routes.UtrYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utr.checkYourAnswersLabel",
              Html(utr),
              changeUrl = Some(controllers.register.business.routes.UtrController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel",
              Html("No"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceInTheUkYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidence.checkYourAnswersLabel",
              Html("France"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceController.onPageLoad(index, draftId).url
              ),
              labelArg = name
            )
          ),
          headingArgs = Seq(1)
        )
      }

      "name, Non-UK residency, UK address" in {
        val userAnswers = emptyUserAnswers
          .set(NamePage(index), name)
          .success
          .value
          .set(UtrYesNoPage(index), true)
          .success
          .value
          .set(UtrPage(index), utr)
          .success
          .value
          .set(CountryOfResidenceYesNoPage(index), true)
          .success
          .value
          .set(CountryOfResidenceInTheUkYesNoPage(index), false)
          .success
          .value
          .set(CountryOfResidencePage(index), "FR")
          .success
          .value
          .set(AddressYesNoPage(index), true)
          .success
          .value
          .set(AddressUkYesNoPage(index), true)
          .success
          .value
          .set(UkAddressPage(index), UkAddress("Line 1", "Line 2", postcode = "NE981ZZ"))
          .success
          .value

        val result = helper.printSection(userAnswers, Some(name), index, draftId)
        result mustBe AnswerSection(
          headingKey = Some("answerPage.section.businessProtector.subheading"),
          rows = Seq(
            AnswerRow(
              "businessProtector.name.checkYourAnswersLabel",
              Html("Business Name"),
              changeUrl = Some(controllers.register.business.routes.NameController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utrYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(controllers.register.business.routes.UtrYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utr.checkYourAnswersLabel",
              Html(utr),
              changeUrl = Some(controllers.register.business.routes.UtrController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel",
              Html("No"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceInTheUkYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidence.checkYourAnswersLabel",
              Html("France"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceController.onPageLoad(index, draftId).url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.addressYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl =
                Some(controllers.register.business.routes.AddressYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.addressUkYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl =
                Some(controllers.register.business.routes.AddressUkYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "site.address.uk.checkYourAnswersLabel",
              Html("Line 1<br />Line 2<br />NE981ZZ"),
              changeUrl = Some(controllers.register.business.routes.UkAddressController.onPageLoad(index, draftId).url),
              labelArg = name
            )
          ),
          headingArgs = Seq(1)
        )
      }

      "name, Non-UK residency, Non-UK address" in {
        val userAnswers = emptyUserAnswers
          .set(NamePage(index), name)
          .success
          .value
          .set(UtrYesNoPage(index), true)
          .success
          .value
          .set(UtrPage(index), utr)
          .success
          .value
          .set(CountryOfResidenceYesNoPage(index), true)
          .success
          .value
          .set(CountryOfResidenceInTheUkYesNoPage(index), false)
          .success
          .value
          .set(CountryOfResidencePage(index), "FR")
          .success
          .value
          .set(AddressYesNoPage(index), true)
          .success
          .value
          .set(AddressUkYesNoPage(index), false)
          .success
          .value
          .set(NonUkAddressPage(index), InternationalAddress("Line 1", "Line 2", country = "FR"))
          .success
          .value

        val result = helper.printSection(userAnswers, Some(name), index, draftId)
        result mustBe AnswerSection(
          headingKey = Some("answerPage.section.businessProtector.subheading"),
          rows = Seq(
            AnswerRow(
              "businessProtector.name.checkYourAnswersLabel",
              Html("Business Name"),
              changeUrl = Some(controllers.register.business.routes.NameController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utrYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(controllers.register.business.routes.UtrYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utr.checkYourAnswersLabel",
              Html(utr),
              changeUrl = Some(controllers.register.business.routes.UtrController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel",
              Html("No"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceInTheUkYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidence.checkYourAnswersLabel",
              Html("France"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceController.onPageLoad(index, draftId).url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.addressYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl =
                Some(controllers.register.business.routes.AddressYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.addressUkYesNo.checkYourAnswersLabel",
              Html("No"),
              changeUrl =
                Some(controllers.register.business.routes.AddressUkYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "site.address.international.checkYourAnswersLabel",
              Html("Line 1<br />Line 2<br />France"),
              changeUrl =
                Some(controllers.register.business.routes.NonUkAddressController.onPageLoad(index, draftId).url),
              labelArg = name
            )
          ),
          headingArgs = Seq(1)
        )
      }

      "name, Non-UK residency, No address" in {
        val userAnswers = emptyUserAnswers
          .set(NamePage(index), name)
          .success
          .value
          .set(UtrYesNoPage(index), true)
          .success
          .value
          .set(UtrPage(index), utr)
          .success
          .value
          .set(CountryOfResidenceYesNoPage(index), true)
          .success
          .value
          .set(CountryOfResidenceInTheUkYesNoPage(index), false)
          .success
          .value
          .set(CountryOfResidencePage(index), "FR")
          .success
          .value
          .set(AddressYesNoPage(index), false)
          .success
          .value

        val result = helper.printSection(userAnswers, Some(name), index, draftId)
        result mustBe AnswerSection(
          headingKey = Some("answerPage.section.businessProtector.subheading"),
          rows = Seq(
            AnswerRow(
              "businessProtector.name.checkYourAnswersLabel",
              Html("Business Name"),
              changeUrl = Some(controllers.register.business.routes.NameController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utrYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(controllers.register.business.routes.UtrYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.utr.checkYourAnswersLabel",
              Html(utr),
              changeUrl = Some(controllers.register.business.routes.UtrController.onPageLoad(index, draftId).url),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceYesNo.checkYourAnswersLabel",
              Html("Yes"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidenceInTheUkYesNo.checkYourAnswersLabel",
              Html("No"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceInTheUkYesNoController
                  .onPageLoad(index, draftId)
                  .url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.5mld.countryOfResidence.checkYourAnswersLabel",
              Html("France"),
              changeUrl = Some(
                controllers.register.business.mld5.routes.CountryOfResidenceController.onPageLoad(index, draftId).url
              ),
              labelArg = name
            ),
            AnswerRow(
              "businessProtector.addressYesNo.checkYourAnswersLabel",
              Html("No"),
              changeUrl =
                Some(controllers.register.business.routes.AddressYesNoController.onPageLoad(index, draftId).url),
              labelArg = name
            )
          ),
          headingArgs = Seq(1)
        )
      }

    }

  }

}
