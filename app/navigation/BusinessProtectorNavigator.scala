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
import pages.register.business.{NamePage, UtrYesNoPage}
import play.api.mvc.Call
import controllers.register.business.{routes => brts}

class BusinessProtectorNavigator @Inject()(config: FrontendAppConfig) extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call = routes(draftId)(page)(userAnswers)

  private def simpleNavigation(draftId: String): PartialFunction[Page, Call] = {
    case NamePage(index) => brts.UtrYesNoController.onPageLoad(index, draftId)
  }

  private def yesNoNavigation(draftId: String) : PartialFunction[Page, ReadableUserAnswers => Call] = {
    case UtrYesNoPage(index) => ua =>
      yesNoNav(
        ua,
        UtrYesNoPage(index),
        brts.UtrYesNoController.onPageLoad(index, draftId),  // TODO
        brts.UtrYesNoController.onPageLoad(index, draftId))  // TODO
  }

  private def routes(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    simpleNavigation(draftId) andThen (c => (_:ReadableUserAnswers) => c) orElse
      yesNoNavigation(draftId)
  }

}

