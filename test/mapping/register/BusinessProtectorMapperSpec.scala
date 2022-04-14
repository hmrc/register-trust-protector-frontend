/*
 * Copyright 2022 HM Revenue & Customs
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
import models.{AddressType, IdentificationOrgType, InternationalAddress, ProtectorCompany, UkAddress}
import org.scalatest.{MustMatchers, OptionValues}
import pages.register.business._
import pages.register.business.mld5.{CountryOfResidenceInTheUkYesNoPage, CountryOfResidencePage, CountryOfResidenceYesNoPage}

class BusinessProtectorMapperSpec extends SpecBase with MustMatchers
  with OptionValues with Generators {

  private val mapper = injector.instanceOf[BusinessProtectorMapper]
  private val index0 = 0
  private val index1 = 1

  "BusinessProtectorMapper" when {

    "when user answers is empty" must {

      "must return None" in {

        val userAnswers = emptyUserAnswers

        mapper.build(userAnswers) mustNot be(defined)
      }
    }

    "when user answers is not empty" must {

      "map data" when {

        "name, utr is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), "Business Name").success.value
              .set(UtrYesNoPage(index0), true).success.value
              .set(UtrPage(index0), "1234567890").success.value

          val businessProtectors = mapper.build(userAnswers)

          businessProtectors mustBe defined
          businessProtectors.value.head mustBe ProtectorCompany(
            name = "Business Name",
            identification = Some(IdentificationOrgType(utr = Some("1234567890"), address = None)),
            countryOfResidence = None
          )
        }

        "name, utr, residency GB is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), "Business Name").success.value
              .set(UtrYesNoPage(index0), true).success.value
              .set(UtrPage(index0), "JP121212A").success.value
              .set(CountryOfResidenceYesNoPage(index0), true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage(index0), true).success.value
              .set(CountryOfResidencePage(index0), "GB").success.value

          val businessProtectors = mapper.build(userAnswers)

          businessProtectors mustBe defined
          businessProtectors.value.head mustBe ProtectorCompany(
            name = "Business Name",
            identification = Some(IdentificationOrgType(utr = Some("JP121212A"), address = None)),
            countryOfResidence = Some("GB")
          )
        }

        "name, utr, residency country is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), "Business Name").success.value
              .set(UtrYesNoPage(index0), true).success.value
              .set(UtrPage(index0), "JP121212A").success.value
              .set(CountryOfResidenceYesNoPage(index0), true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage(index0), false).success.value
              .set(CountryOfResidencePage(index0), "FR").success.value

          val businessProtectors = mapper.build(userAnswers)

          businessProtectors mustBe defined
          businessProtectors.value.head mustBe ProtectorCompany(
            name = "Business Name",
            identification = Some(IdentificationOrgType(utr = Some("JP121212A"), address = None)),
            countryOfResidence = Some("FR")
          )
        }

        "name, UK Address is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), "Business Name").success.value
              .set(UtrYesNoPage(index0), false).success.value
              .set(AddressYesNoPage(index0), true).success.value
              .set(AddressUkYesNoPage(index0), true).success.value
              .set(UkAddressPage(index0),
                UkAddress("Line1", "Line2", Some("Line3"), Some("Newcastle"), "NE62RT")).success.value

          val businessProtectors = mapper.build(userAnswers)

          businessProtectors mustBe defined
          businessProtectors.value.head mustBe ProtectorCompany(
            name = "Business Name",
            identification = Some(IdentificationOrgType(utr = None,
              address = Some(
                AddressType("Line1", "Line2", Some("Line3"), Some("Newcastle"), Some("NE62RT"), "GB")
              )
            )),
            countryOfResidence = None
          )
        }

        "name, International Address is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), "Business Name").success.value
              .set(UtrYesNoPage(index0), false).success.value
              .set(AddressYesNoPage(index0), true).success.value
              .set(AddressUkYesNoPage(index0), false).success.value
              .set(NonUkAddressPage(index0),
                InternationalAddress("Line1", "Line2", Some("Line3"), "US")).success.value

          val businessProtectors = mapper.build(userAnswers)

          businessProtectors mustBe defined
          businessProtectors.value.head mustBe ProtectorCompany(
            name = "Business Name",
            identification = Some(IdentificationOrgType(
              utr = None,
              address = Some(
                AddressType("Line1", "Line2", Some("Line3"), None, None, "US")
              )
            )),
            countryOfResidence = None
          )
        }

        "name, residency, address is set" in {
          val userAnswers =
            emptyUserAnswers
              .set(NamePage(index0), "Business Name").success.value
              .set(UtrYesNoPage(index0), false).success.value
              .set(CountryOfResidenceYesNoPage(index0), true).success.value
              .set(CountryOfResidenceInTheUkYesNoPage(index0), false).success.value
              .set(CountryOfResidencePage(index0), "FR").success.value
              .set(AddressYesNoPage(index0), true).success.value
              .set(AddressUkYesNoPage(index0), true).success.value
              .set(UkAddressPage(index0),
                UkAddress("Line1", "Line2", Some("Line3"), Some("Newcastle"), "NE62RT")).success.value

          val businessProtectors = mapper.build(userAnswers)

          businessProtectors mustBe defined
          businessProtectors.value.head mustBe ProtectorCompany(
            name = "Business Name",
            identification = Some(IdentificationOrgType(utr = None,
              address = Some(
                AddressType("Line1", "Line2", Some("Line3"), Some("Newcastle"), Some("NE62RT"), "GB")
              )
            )),
            countryOfResidence = Some("FR")
          )
        }

      }

      "must be able to create multiple Business Protectors" in {
        val userAnswers =
          emptyUserAnswers
            .set(NamePage(index0), "Business Name 1").success.value
            .set(UtrYesNoPage(index0), true).success.value
            .set(UtrPage(index0), "1234567890").success.value

            .set(NamePage(index1), "Business Name 2").success.value
            .set(UtrYesNoPage(index1), false).success.value
            .set(AddressYesNoPage(index1), true).success.value
            .set(AddressUkYesNoPage(index1), true).success.value
            .set(UkAddressPage(index1),
              UkAddress("Line1", "Line2", Some("Line3"), Some("Newcastle"), "NE62RT")).success.value


        val businessProtectors = mapper.build(userAnswers)

        businessProtectors mustBe defined
        businessProtectors.value mustBe
          List(
            ProtectorCompany(
              name = "Business Name 1",
              identification = Some(IdentificationOrgType(utr = Some("1234567890"), address = None)),
              countryOfResidence = None
            ),
            ProtectorCompany(
              name = "Business Name 2",
              identification = Some(
                IdentificationOrgType(
                  utr = None,
                  address = Some(
                    AddressType("Line1", "Line2", Some("Line3"), Some("Newcastle"), Some("NE62RT"), "GB")
                  ))
              ),
              countryOfResidence = None
            )
          )
      }

    }
  }
}

