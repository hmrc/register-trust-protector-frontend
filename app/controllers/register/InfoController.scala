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
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.{InfoView, InfoView5MLD}

import scala.concurrent.ExecutionContext

class InfoController @Inject()(
                                override val messagesApi: MessagesApi,
                                standardActionSets: StandardActionSets,
                                val controllerComponents: MessagesControllerComponents,
                                view: InfoView,
                                view5MLD: InfoView5MLD,
                                featureFlagService: FeatureFlagService
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).async {
    implicit request =>
      featureFlagService.is5mldEnabled().map {
        case true =>
          Ok(view5MLD(draftId))
        case _ =>
          Ok(view(draftId))
      }

  }
}
