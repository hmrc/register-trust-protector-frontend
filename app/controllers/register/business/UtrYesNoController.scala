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
import controllers.actions.StandardActionSets
import controllers.actions.register.business.NameRequiredAction
import forms.YesNoFormProvider
import javax.inject.Inject
import navigation.Navigator
import pages.register.business.UtrYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.business.UtrYesNoView

import scala.concurrent.{ExecutionContext, Future}

class UtrYesNoController @Inject()(
                                    override val messagesApi: MessagesApi,
                                    repository: RegistrationsRepository,
                                    @BusinessProtector navigator: Navigator,
                                    standardActionSets: StandardActionSets,
                                    nameAction: NameRequiredAction,
                                    formProvider: YesNoFormProvider,
                                    val controllerComponents: MessagesControllerComponents,
                                    view: UtrYesNoView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider.withPrefix("businessProtector.utrYesNo")

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).andThen(nameAction(index)) {
    implicit request =>

      val preparedForm = request.userAnswers.get(UtrYesNoPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.protectorName, index, draftId))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).andThen(nameAction(index)).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, request.protectorName, index, draftId))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(UtrYesNoPage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(UtrYesNoPage(index), draftId, updatedAnswers))
      )
  }
}
