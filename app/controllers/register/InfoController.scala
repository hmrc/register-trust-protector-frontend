/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.actions.StandardActionSets
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.{InfoView, InfoView5MLD, InfoViewNonTaxable}

import javax.inject.Inject

class InfoController @Inject()(
                                override val messagesApi: MessagesApi,
                                standardActionSets: StandardActionSets,
                                val controllerComponents: MessagesControllerComponents,
                                view: InfoView,
                                view5MLD: InfoView5MLD,
                                viewNonTaxable: InfoViewNonTaxable
                              ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId) {
    implicit request =>
      
      val userAnswers: UserAnswers = request.userAnswers

      (userAnswers.is5mldEnabled, userAnswers.isTaxable) match {
        case (true, true) =>
          Ok(view5MLD(draftId))
        case (true, false) =>
          Ok(viewNonTaxable(draftId))
        case _ =>
          Ok(view(draftId))
      }
  }

  def onSubmit(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId) { _ =>
    Redirect(controllers.register.routes.IndividualOrBusinessController.onPageLoad(draftId))
  }
}
