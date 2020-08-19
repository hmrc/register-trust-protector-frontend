/*
 * Copyright 2020 HM Revenue & Customs
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

package forms.behaviours

import java.time.LocalDate

import forms.{FormSpec, Validation}
import generators.Generators
import models.PassportOrIdCardDetails
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

trait PassportOrIDCardBehaviours extends FormSpec
  with ScalaCheckPropertyChecks with Generators with FieldBehaviours with OptionalFieldBehaviours {

  def passportOrIDCardDateField(form: Form[PassportOrIdCardDetails], key: String): Unit = {

    "bind a valid date" in {

      val generator = datesBetween(LocalDate.of(1500, 1, 1), LocalDate.of(2099, 12, 31))

      forAll(generator -> "valid dates") {
        date =>

          val data = Map(
            s"$key.day"   -> date.getDayOfMonth.toString,
            s"$key.month" -> date.getMonthValue.toString,
            s"$key.year"  -> date.getYear.toString
          )

          val result = form.bind(data).apply("expiryDate")

          result.errors shouldBe empty
      }
    }
  }

  def passportOrIdCardNumberField(form: Form[_],
                          fieldName: String,
                          invalidError: FormError): Unit = {

    s"not bind strings which do not match valid passport or id card number format " in {
      val generator = stringsWithMaxLength(30)
      forAll(generator) {
        string =>
          whenever(!string.matches(Validation.passportOrIdCardNumberRegEx)) {
            val result = form.bind(Map(fieldName -> string)).apply(fieldName)
            result.errors shouldEqual Seq(invalidError)
          }
      }
    }
  }

  def mandatoryPassportOrIdDateField(form: Form[PassportOrIdCardDetails], key: String, requiredAllKey: String): Unit = {

    "fail to bind an empty date" in {

      val result = form.bind(Map.empty[String, String]).apply(key)

      result.errors should contain(FormError(key, requiredAllKey, List.empty))
    }
  }


  def passportOrIDCardInvalidDateField(form: Form[PassportOrIdCardDetails], key: String, requiredAllKey: String): Unit = {

    "not bind an invalid date" in {

      val data = Map(
        s"$key.day"   -> "4wafq5",
        s"$key.month" -> "1asda3",
        s"$key.year"  -> "20asdfq41"
      )

      val result = form.bind(data).apply("expiryDate")

      result.errors should contain(FormError(key, requiredAllKey, List.empty))
    }
  }


}