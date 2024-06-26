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

package views.register.business

import forms.UtrFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.register.business.UtrView

class UtrViewSpec extends StringViewBehaviours {

  private val index = 0
  val messageKeyPrefix = "businessProtector.utr"
  val name = "Name"

  override val form: Form[String] = new UtrFormProvider().withConfig(messageKeyPrefix, emptyUserAnswers, index)

  "Utr view" must {

    val view = viewFor[UtrView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, name, index, draftId)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name, "hint", "p1", "link")

    behave like pageWithHint(form, applyView, messageKeyPrefix + ".hint")

    behave like stringPage(form, applyView, messageKeyPrefix, Some(name), controllers.register.business.routes.UtrController.onSubmit(index, fakeDraftId).url)

    behave like pageWithBackLink(applyView(form))
  }
}
