/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.register.individual

import config.annotations.IndividualProtector
import controllers.actions._
import controllers.actions.register.individual.NameRequiredAction
import forms.NationalInsuranceNumberFormProvider

import javax.inject.Inject
import navigation.Navigator
import pages.register.individual.NationalInsuranceNumberPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import services.DraftRegistrationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.register.individual.NationalInsuranceNumberView

import scala.concurrent.{ExecutionContext, Future}

class NationalInsuranceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  registrationsRepository: RegistrationsRepository,
  @IndividualProtector navigator: Navigator,
  standardActionSets: StandardActionSets,
  nameAction: NameRequiredAction,
  formProvider: NationalInsuranceNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NationalInsuranceNumberView,
  draftRegistrationService: DraftRegistrationService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private def getForm(draftId: String, index: Int)(implicit
    request: ProtectorNameRequest[AnyContent]
  ): Future[Form[String]] =
    for {
      existingSettlorNinos <- getSettlorNinos(draftId)
    } yield formProvider.withPrefix(
      "individualProtector.nationalInsuranceNumber",
      request.userAnswers,
      index,
      Seq(existingSettlorNinos)
    )

  private def getSettlorNinos(draftId: String)(implicit request: ProtectorNameRequest[AnyContent]) =
    draftRegistrationService.retrieveSettlorNinos(draftId)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(draftId).andThen(nameAction(index)).async { implicit request =>
      getForm(draftId, index).map { form =>
        val preparedForm = request.userAnswers.get(NationalInsuranceNumberPage(index)) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, request.protectorName, index, draftId))
      }
    }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] =
    standardActionSets.identifiedUserWithData(draftId).andThen(nameAction(index)).async { implicit request =>
      getForm(draftId, index).flatMap { form =>
        form
          .bindFromRequest()
          .fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(view(formWithErrors, request.protectorName, index, draftId))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(NationalInsuranceNumberPage(index), value))
                _              <- registrationsRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(NationalInsuranceNumberPage(index), draftId, updatedAnswers))
          )
      }
    }

}
