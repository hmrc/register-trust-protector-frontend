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

package controllers.register.individual.mld5

import config.annotations.IndividualProtector
import controllers.actions._
import controllers.actions.register.individual.NameRequiredAction
import forms.YesNoDontKnowFormProvider
import models.YesNoDontKnow
import navigation.Navigator
import pages.register.individual.mld5.LegallyCapableYesNoPage
import play.api.data.Form
import play.api.i18n._
import play.api.mvc._
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.individual.mld5.LegallyCapableYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LegallyCapableYesNoController @Inject()(
                                                   val controllerComponents: MessagesControllerComponents,
                                                   repository: RegistrationsRepository,
                                                   @IndividualProtector navigator: Navigator,
                                                   standardActionSets: StandardActionSets,
                                                   nameAction: NameRequiredAction,
                                                   formProvider: YesNoDontKnowFormProvider,
                                                   view: LegallyCapableYesNoView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[YesNoDontKnow] = formProvider.withPrefix("individualProtector.5mld.legallyCapableYesNo")

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(draftId).andThen(nameAction(index)) {
      implicit request =>

        val preparedForm = request.userAnswers.get(LegallyCapableYesNoPage(index)) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm,  draftId , index, request.protectorName))
    }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(draftId).andThen(nameAction(index)).async {
      implicit request =>

        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, draftId , index, request.protectorName))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(LegallyCapableYesNoPage(index), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(LegallyCapableYesNoPage(index), draftId, updatedAnswers))
        )
    }
}
