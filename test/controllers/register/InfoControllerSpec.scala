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

package controllers.register

import base.SpecBase
import models.UserAnswers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.register.{InfoView, InfoViewNonTaxable}

class InfoControllerSpec extends SpecBase {

  "Info Controller" must {

    "return OK and the correct view for a GET" when {

      "taxable" in {

        val answers: UserAnswers = emptyUserAnswers.copy(isTaxable = true)

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, routes.InfoController.onPageLoad(fakeDraftId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InfoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(fakeDraftId)(request, messages).toString

        application.stop()
      }

      "non taxable" in {

        val answers: UserAnswers = emptyUserAnswers.copy(isTaxable = false)

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, routes.InfoController.onPageLoad(fakeDraftId).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InfoViewNonTaxable]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(fakeDraftId)(request, messages).toString

        application.stop()
      }
    }
  }
}
