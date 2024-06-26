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
import forms.StringFormProvider
import javax.inject.Inject
import navigation.Navigator
import pages.register.business.NamePage
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.business.NameView

import scala.concurrent.{ExecutionContext, Future}

class NameController @Inject()(
                                val controllerComponents: MessagesControllerComponents,
                                standardActionSets: StandardActionSets,
                                formProvider: StringFormProvider,
                                repository: RegistrationsRepository,
                                view: NameView,
                                @BusinessProtector navigator: Navigator
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[String] = formProvider.withPrefix("businessProtector.name", 105)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(NamePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, index, draftId))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index, draftId))),
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NamePage(index), value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(NamePage(index), draftId, updatedAnswers))
        }
      )
  }
}
