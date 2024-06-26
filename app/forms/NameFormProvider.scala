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

package forms

import forms.helpers.WhitespaceHelper._
import forms.mappings.Mappings

import javax.inject.Inject
import models.FullName
import play.api.data.Form
import play.api.data.Forms._

class NameFormProvider @Inject() extends Mappings {

   def withPrefix(prefix: String): Form[FullName] =   Form(
    mapping(
      "firstName" -> text(s"$prefix.error.firstname.required")
        .verifying(
          firstError(
            maxLength(35, s"$prefix.error.firstname.length"),
            nonEmptyString("firstName", s"$prefix.error.firstname.required"),
            regexp(Validation.nameRegex, s"$prefix.error.firstname.invalid")
          )
        ),
      "middleName" -> optional(text()
        .transform(trimWhitespace, identity[String])
        .verifying(
          firstError(
            maxLength(35, s"$prefix.error.middlename.length"),
            regexp(Validation.nameRegex, s"$prefix.error.middlename.invalid"))
        )
      ).transform(emptyToNone, identity[Option[String]]),
      "lastName" -> text(s"$prefix.error.lastname.required")
        .verifying(
          firstError(
            maxLength(35, s"$prefix.error.lastname.length"),
            nonEmptyString("lastName", s"$prefix.error.lastname.required"),
            regexp(Validation.nameRegex, s"$prefix.error.lastname.invalid")
          )
        )
    )(FullName.apply)(FullName.unapply)
   )
 }
