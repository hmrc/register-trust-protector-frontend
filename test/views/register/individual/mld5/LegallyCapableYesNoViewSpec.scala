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

package views.register.individual.mld5

import forms.YesNoDontKnowFormProvider
import models.YesNoDontKnow
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.RadioOption
import views.behaviours.{OptionsViewBehaviours, QuestionViewBehaviours}
import views.html.register.individual.mld5.LegallyCapableYesNoView

class LegallyCapableYesNoViewSpec extends QuestionViewBehaviours[YesNoDontKnow] with OptionsViewBehaviours {

  val prefix = "individualProtector.5mld.legallyCapableYesNo"
  val index = 0
  val name = "Test"

  val form: Form[YesNoDontKnow] = new YesNoDontKnowFormProvider().withPrefix(prefix)

  "legallyCapableYesNo view" must {

    val view = viewFor[LegallyCapableYesNoView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, fakeDraftId, index, name)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), prefix, name, "p1", "bulletpoint1", "bulletpoint2", "bulletpoint3", "bulletpoint4")

    behave like pageWithBackLink(applyView(form))

    val options = List(
      RadioOption(id = "value-yes", value = YesNoDontKnow.Yes.toString, messageKey = "site.yes"),
      RadioOption(id = "value-no", value = YesNoDontKnow.No.toString, messageKey = "site.no"),
      RadioOption(id = "value-dontKnow", value = YesNoDontKnow.DontKnow.toString, messageKey = "site.dontKnow")
    )

    behave like pageWithOptions[YesNoDontKnow](form, applyView, options)

    behave like pageWithASubmitButton(applyView(form))
  }
}
