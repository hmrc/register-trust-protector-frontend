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

package controllers.register.business

import config.annotations.BusinessProtector
import controllers.actions.register.business.NameRequiredAction
import controllers.actions.{ProtectorNameRequest, StandardActionSets}
import forms.UtrFormProvider
import navigation.Navigator
import pages.register.business.UtrPage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.business.UtrView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UtrController @Inject()(
                               val controllerComponents: MessagesControllerComponents,
                               standardActionSets: StandardActionSets,
                               nameAction: NameRequiredAction,
                               formProvider: UtrFormProvider,
                               repository: RegistrationsRepository,
                               view: UtrView,
                               @BusinessProtector navigator: Navigator
                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(index: Int)(implicit request: ProtectorNameRequest[AnyContent]): Form[String] =
    formProvider.withConfig("businessProtector.utr", request.userAnswers, index)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).andThen(nameAction(index)) {
    implicit request =>

      val preparedForm = request.userAnswers.get(UtrPage(index)) match {
        case None => form(index)
        case Some(value) => form(index).fill(value)
      }

      Ok(view(preparedForm, request.protectorName, index, draftId))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).andThen(nameAction(index)).async {
    implicit request =>
      form(index).bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, request.protectorName, index, draftId))),
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(UtrPage(index), value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(UtrPage(index), draftId, updatedAnswers))
        }
      )
  }
}
