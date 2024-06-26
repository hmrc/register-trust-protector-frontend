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

package controllers

import connectors.SubmissionDraftConnector
import controllers.actions.register.RegistrationIdentifierAction
import controllers.register.{routes => rts}
import models.TaskStatus.InProgress
import models.UserAnswers
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import services.TrustsStoreService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 repository: RegistrationsRepository,
                                 identify: RegistrationIdentifierAction,
                                 trustsStoreService: TrustsStoreService,
                                 submissionDraftConnector: SubmissionDraftConnector
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(draftId: String): Action[AnyContent] = identify.async { implicit request =>

    for {
      isTaxable <- submissionDraftConnector.getIsTrustTaxable(draftId)
      utr <- submissionDraftConnector.getTrustUtr(draftId)
      userAnswers <- repository.get(draftId)
      ua = userAnswers match {
        case Some(value) => value.copy(isTaxable = isTaxable, existingTrustUtr = utr)
        case _ => UserAnswers(draftId, Json.obj(), request.identifier, isTaxable, utr)
      }
      _ <- repository.set(ua)
      _ <- trustsStoreService.updateTaskStatus(draftId, InProgress)
    } yield {
      if (ua.isAnyProtectorAdded) {
        Redirect(rts.AddAProtectorController.onPageLoad(draftId))
      } else {
        Redirect(rts.TrustHasProtectorYesNoController.onPageLoad(draftId))
      }
    }
  }
}
