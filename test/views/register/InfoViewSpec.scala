/*
 * Copyright 2026 HM Revenue & Customs
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

package views.register

import views.behaviours.ViewBehaviours
import views.html.register.InfoView

class InfoViewSpec extends ViewBehaviours {

  "Info view" must {

    val view = viewFor[InfoView](Some(emptyUserAnswers))

    val applyView = view.apply(fakeDraftId)(fakeRequest, messages)

    behave like normalPageTitleWithSectionSubheading(
      applyView,
      "protectorsInfo.5mld.taxable",
      "caption",
      "subheading1",
      "paragraph1",
      "individual.bulletpoint1",
      "individual.bulletpoint2",
      "individual.bulletpoint3",
      "individual.bulletpoint4",
      "individual.bulletpoint5",
      "individual.bulletpoint6",
      "individual.mental.capacity.p1",
      "individual.mental.capacity.p2",
      "individual.mental.capacity.bulletpoint1",
      "individual.mental.capacity.bulletpoint2",
      "individual.mental.capacity.bulletpoint3",
      "individual.mental.capacity.bulletpoint4",
      "subheading2",
      "paragraph3",
      "business.bulletpoint4",
      "business.bulletpoint5",
      "details.what.we.mean",
      "details.individual.heading",
      "details.individual.paragraph1",
      "details.business.heading",
      "details.business.paragraph1",
      "details.business.paragraph2"
    )

    behave like pageWithBackLink(applyView)

  }

}
