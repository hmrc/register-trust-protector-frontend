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

package controllers

import connectors.SubmissionDraftConnector
import controllers.actions.register.RegistrationIdentifierAction
import controllers.register.AnyProtectors
import javax.inject.Inject
import models.UserAnswers
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import controllers.register.{routes => rts}
import services.FeatureFlagService

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 repository: RegistrationsRepository,
                                 identify: RegistrationIdentifierAction,
                                 featureFlagService: FeatureFlagService,
                                 submissionDraftConnector: SubmissionDraftConnector
                               ) extends FrontendBaseController with I18nSupport with AnyProtectors {

  implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  def onPageLoad(draftId: String): Action[AnyContent] = identify.async { implicit request =>

    featureFlagService.is5mldEnabled() flatMap {
      is5mldEnabled =>
        submissionDraftConnector.getIsTrustTaxable(draftId) flatMap {
          isTaxable =>
            repository.get(draftId) flatMap {
              case Some(userAnswers) =>
                val ua = userAnswers.copy(is5mldEnabled = is5mldEnabled, isTaxable = isTaxable)
                Future.successful(redirect(ua, draftId))
              case _ =>
                val userAnswers = UserAnswers(draftId, Json.obj(), request.identifier, is5mldEnabled, isTaxable)
                repository.set(userAnswers) map {
                  _ => redirect(userAnswers, draftId)
                }
            }
        }
    }
  }

  private def redirect(userAnswers: UserAnswers, draftId: String) = {
    if (isAnyProtectorAdded(userAnswers)) {
      Redirect(rts.AddAProtectorController.onPageLoad(draftId))
    } else {
      Redirect(rts.TrustHasProtectorYesNoController.onPageLoad(draftId))
    }
  }
}
