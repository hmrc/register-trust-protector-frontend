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

package mapping.register

import base.SpecBase
import generators.Generators
import mapping.reads.IndividualProtector
import models.{AddressType, FullName, IdentificationType, InternationalAddress, PassportOrIdCardDetails, PassportType, Protector, UkAddress, YesNoDontKnow}
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import pages.register.individual._
import pages.register.individual.mld5._
import play.api.libs.json.Json

import java.time.LocalDate

class IndividualProtectorMapperSpec extends SpecBase with Matchers
  with OptionValues with Generators {

  private val mapper = injector.instanceOf[IndividualProtectorMapper]
  private val index0 = 0
  private val index1 = 1
  private val firstName = "first"
  private val lastName = "last"
  private val nino = "AB123456C"
  private val dateOfBirth = LocalDate.of(2000, 1, 1)
  private val passportExpiry = LocalDate.of(2025, 1, 1)

  "IndividualProtectorMapper" when {

    "when user answers is empty" must {

      "must return None" in {

        val userAnswers = emptyUserAnswers

        mapper.build(userAnswers) mustNot be(defined)
      }
    }

    "when user answers is not empty" must {

      "IndividualProtector reads" must {

        "parse the old mental capacity question" in {

          val json = Json.parse(
            """
              |{
              | "name": {
              |   "firstName": "John",
              |   "lastName": "Smith"
              | },
              | "legallyCapable": true
              |}
              |""".stripMargin)

          json.as[IndividualProtector] mustBe IndividualProtector(
            name = FullName("John", None, "Smith"),
            dateOfBirth = None,
            nationalInsuranceNumber = None,
            ukAddress = None,
            internationalAddress = None,
            passportDetails = None,
            idCardDetails = None,
            countryOfResidence = None,
            nationality = None,
            legallyCapable = Some(YesNoDontKnow.Yes)
          )
        }

        "parse the new mental capacity question" in {
          val json = Json.parse(
            """
              |{
              | "name": {
              |   "firstName": "John",
              |   "lastName": "Smith"
              | },
              | "legallyCapable": "dontKnow"
              |}
              |""".stripMargin)

          json.as[IndividualProtector] mustBe IndividualProtector(
            name = FullName("John", None, "Smith"),
            dateOfBirth = None,
            nationalInsuranceNumber = None,
            ukAddress = None,
            internationalAddress = None,
            passportDetails = None,
            idCardDetails = None,
            countryOfResidence = None,
            nationality = None,
            legallyCapable = Some(YesNoDontKnow.DontKnow)
          )
        }

      }

      "return mapped data" when {

        "nino is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), FullName(firstName, None, lastName)).success.value
              .set(DateOfBirthYesNoPage(index0), false).success.value
              .set(NationalInsuranceYesNoPage(index0), true).success.value
              .set(NationalInsuranceNumberPage(index0), nino).success.value

          val individualProtectors = mapper.build(userAnswers)

          individualProtectors mustBe defined
          individualProtectors.value.head mustBe Protector(
            name = FullName(firstName, None, lastName),
            dateOfBirth = None,
            identification = Some(IdentificationType(nino = Some(nino), address = None, passport = None)),
            None,
            None,
            None
          )
        }

        "UK Address is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), FullName(firstName, None, lastName)).success.value
              .set(DateOfBirthYesNoPage(index0), false).success.value
              .set(NationalInsuranceYesNoPage(index0), false).success.value
              .set(AddressYesNoPage(index0), true).success.value
              .set(AddressUkYesNoPage(index0), true).success.value
              .set(UkAddressPage(index0), UkAddress("Line1", "Line2", Some("Line3"), Some("Newcastle"), "NE62RT")).success.value
              .set(PassportDetailsYesNoPage(index0), false).success.value
              .set(IDCardDetailsYesNoPage(index0), false).success.value

          val individualProtectors = mapper.build(userAnswers)

          individualProtectors mustBe defined
          individualProtectors.value.head mustBe Protector(
            name = FullName(firstName, None, lastName),
            identification = Some(IdentificationType(nino = None,
              address = Some(AddressType("Line1", "Line2", Some("Line3"), Some("Newcastle"), Some("NE62RT"), "GB")),
              passport = None
            )),
            dateOfBirth = None,
            countryOfResidence = None,
            nationality = None,
            legallyIncapable = None
          )
        }

        "International Address is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), FullName(firstName, None, lastName)).success.value
              .set(DateOfBirthYesNoPage(index0), false).success.value
              .set(NationalInsuranceYesNoPage(index0), false).success.value
              .set(AddressYesNoPage(index0), true).success.value
              .set(AddressUkYesNoPage(index0), false).success.value
              .set(NonUkAddressPage(index0), InternationalAddress("Line1", "Line2", Some("Line3"), "US")).success.value
              .set(PassportDetailsYesNoPage(index0), false).success.value
              .set(IDCardDetailsYesNoPage(index0), false).success.value

          val individualProtectors = mapper.build(userAnswers)

          individualProtectors mustBe defined
          individualProtectors.value.head mustBe Protector(
            name = FullName(firstName, None, lastName),
            identification = Some(IdentificationType(
              nino = None,
              address = Some(AddressType("Line1", "Line2", Some("Line3"), None, None, "US")),
              passport = None
            )),
            dateOfBirth = None,
            countryOfResidence = None,
            nationality = None,
            legallyIncapable = None
          )
        }

        "DateOfBirth, Address and Passport is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), FullName(firstName, None, lastName)).success.value
              .set(DateOfBirthYesNoPage(index0), true).success.value
              .set(DateOfBirthPage(index0), dateOfBirth).success.value
              .set(NationalInsuranceYesNoPage(index0), false).success.value
              .set(AddressYesNoPage(index0), true).success.value
              .set(AddressUkYesNoPage(index0), true).success.value
              .set(UkAddressPage(index0), UkAddress("Line1", "Line2", Some("Line3"), Some("Newcastle"), "NE62RT")).success.value
              .set(PassportDetailsYesNoPage(index0), true).success.value
              .set(PassportDetailsPage(index0), PassportOrIdCardDetails("GB", "12345", passportExpiry)).success.value
              .set(IDCardDetailsYesNoPage(index0), false).success.value

          val individualProtectors = mapper.build(userAnswers)

          individualProtectors mustBe defined
          individualProtectors.value.head mustBe Protector(
            name = FullName(firstName, None, lastName),
            identification = Some(IdentificationType(nino = None,
              address = Some(AddressType("Line1", "Line2", Some("Line3"), Some("Newcastle"), Some("NE62RT"), "GB")),
              passport = Some(PassportType("12345", passportExpiry, "GB"))
            )),
            dateOfBirth = Some(dateOfBirth),
            countryOfResidence = None,
            nationality = None,
            legallyIncapable = None
          )
        }

        "name, residency GB is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), FullName("Name", None, "Protector")).success.value
              .set(CountryOfResidenceYesNoPage(index0), true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage(index0), true).success.value
              .set(CountryOfResidencePage(index0), "GB").success.value

          val individualProtectors = mapper.build(userAnswers)

          individualProtectors mustBe defined
          individualProtectors.value.head mustBe Protector(
            name = FullName("Name", None, "Protector"),
            dateOfBirth = None,
            identification = None,
            countryOfResidence = Some("GB"),
            None,
            None
          )
        }

        "name, residency country is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), FullName("Name", None, "Protector")).success.value
              .set(CountryOfResidenceYesNoPage(index0), true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage(index0), false).success.value
              .set(CountryOfResidencePage(index0), "FR").success.value

          val individualProtectors = mapper.build(userAnswers)

          individualProtectors mustBe defined
          individualProtectors.value.head mustBe Protector(
            name = FullName("Name", None, "Protector"),
            dateOfBirth = None,
            identification = None,
            countryOfResidence = Some("FR"),
            None,
            None
          )
        }

        "name, nationality GB is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), FullName("Name", None, "Protector")).success.value
              .set(NationalityYesNoPage(index0), true).success.value
              .set(NationalityUkYesNoPage(index0), true).success.value
              .set(NationalityPage(index0), "GB").success.value

          val individualProtectors = mapper.build(userAnswers)

          individualProtectors mustBe defined
          individualProtectors.value.head mustBe Protector(
            name = FullName("Name", None, "Protector"),
            dateOfBirth = None,
            identification = None,
            None,
            nationality = Some("GB"),
            None
          )
        }

        "name, nationality country is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), FullName("Name", None, "Protector")).success.value
              .set(NationalityYesNoPage(index0), true).success.value
              .set(NationalityUkYesNoPage(index0), false).success.value
              .set(NationalityPage(index0), "FR").success.value

          val individualProtectors = mapper.build(userAnswers)

          individualProtectors mustBe defined
          individualProtectors.value.head mustBe Protector(
            name = FullName("Name", None, "Protector"),
            dateOfBirth = None,
            identification = None,
            None,
            nationality = Some("FR"),
            None
          )
        }

        "name, legally capable is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), FullName("Name", None, "Protector")).success.value
              .set(NationalityYesNoPage(index0), false).success.value
              .set(CountryOfResidenceYesNoPage(index0), false).success.value
              .set(LegallyCapableYesNoPage(index0), YesNoDontKnow.Yes).success.value

          val individualProtectors = mapper.build(userAnswers)

          individualProtectors mustBe defined
          individualProtectors.value.head mustBe Protector(
            name = FullName("Name", None, "Protector"),
            dateOfBirth = None,
            identification = None,
            None,
            None,
            Some(false)
          )
        }
      }

      "must be able to create multiple Individual Protectors" in {
        val userAnswers =
          emptyUserAnswers
            .set(NamePage(index0), FullName("Individual Name 1", None, lastName)).success.value
            .set(DateOfBirthYesNoPage(index0), false).success.value
            .set(NationalInsuranceYesNoPage(index0), true).success.value
            .set(NationalInsuranceNumberPage(index0), nino).success.value

            .set(NamePage(index1), FullName("Individual Name 2", None, lastName)).success.value
            .set(DateOfBirthYesNoPage(index1), false).success.value
            .set(NationalInsuranceYesNoPage(index1), false).success.value
            .set(AddressYesNoPage(index1), true).success.value
            .set(AddressUkYesNoPage(index1), true).success.value
            .set(UkAddressPage(index1),
              UkAddress("Line1", "Line2", Some("Line3"), Some("Newcastle"), "NE62RT")).success.value
            .set(PassportDetailsYesNoPage(index1), false).success.value
            .set(IDCardDetailsYesNoPage(index1), false).success.value


        val individualProtectors = mapper.build(userAnswers)

        individualProtectors mustBe defined
        individualProtectors.value mustBe
          List(
            Protector(
              name = FullName("Individual Name 1", None, lastName),
              dateOfBirth = None,
              identification = Some(IdentificationType(nino = Some(nino), address = None, passport = None)),
              countryOfResidence = None,
              nationality = None,
              legallyIncapable = None
            ),

            Protector(
              name = FullName("Individual Name 2", None, lastName),
              dateOfBirth = None,
              identification = Some(
                IdentificationType(
                  nino = None,
                  address = Some(AddressType("Line1", "Line2", Some("Line3"), Some("Newcastle"), Some("NE62RT"), "GB")),
                  passport = None
                )
              ),
              countryOfResidence = None,
              nationality = None,
              legallyIncapable = None
            )
          )
      }

    }
  }
}

