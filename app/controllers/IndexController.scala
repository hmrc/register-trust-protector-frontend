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
import controllers.register.{routes => rts}
import models.TaskStatus.InProgress
import models.UserAnswers
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.RegistrationsRepository
import services.TrustsStoreService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 repository: RegistrationsRepository,
                                 identify: RegistrationIdentifierAction,
                                 trustsStoreService: TrustsStoreService,
                                 submissionDraftConnector: SubmissionDraftConnector
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(draftId: String): Action[AnyContent] = identify.async { implicit request =>

    def redirect(userAnswers: UserAnswers): Future[Result] = {
      repository.set(userAnswers) flatMap { _ =>
        if (userAnswers.isAnyProtectorAdded) {
          Future.successful(Redirect(rts.AddAProtectorController.onPageLoad(draftId)))
        } else {
          trustsStoreService.updateTaskStatus(draftId, InProgress) map { _ =>
            Redirect(rts.TrustHasProtectorYesNoController.onPageLoad(draftId))
          }
        }
      }
    }

    for {
      is5mldEnabled <- trustsStoreService.is5mldEnabled()
      isTaxable <- submissionDraftConnector.getIsTrustTaxable(draftId)
      utr <- submissionDraftConnector.getTrustUtr(draftId)
      userAnswers <- repository.get(draftId)
      ua = userAnswers match {
        case Some(value) => value.copy(is5mldEnabled = is5mldEnabled, isTaxable = isTaxable, existingTrustUtr = utr)
        case _ => UserAnswers(draftId, Json.obj(), request.identifier, is5mldEnabled, isTaxable, utr)
      }
      result <- redirect(ua)
    } yield result
  }
}
