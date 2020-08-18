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

package navigation

import config.FrontendAppConfig
import javax.inject.Inject
import models.ReadableUserAnswers
import pages.Page
import pages.register.AnswersPage
import play.api.mvc.Call
import controllers.register.business.{routes => brts}

class ProtectorNavigator @Inject()(config: FrontendAppConfig) extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call =
    route(draftId, config)(page)(userAnswers)

  private def route(draftId: String, config: FrontendAppConfig): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case AnswersPage => _ => brts.CheckDetailsController.onPageLoad(0, draftId) // TODO controllers.register.beneficiaries.routes.AddABeneficiaryController.onPageLoad(draftId)
  }
}

