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

import config.FrontendAppConfig
import controllers.actions.{RequiredAnswer, RequiredAnswerAction, RequiredAnswerActionProvider, StandardActionSets}
import forms.{AddAProtectorFormProvider, YesNoFormProvider}
import models.TaskStatus._
import models.register.pages.AddAProtector
import models.register.pages.AddAProtector._
import navigation.Navigator
import pages.register.{AddAProtectorPage, AddAProtectorYesNoPage, TrustHasProtectorYesNoPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi, MessagesProvider}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.RegistrationsRepository
import services.TrustsStoreService
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AddAProtectorViewHelper
import views.html.register.{AddAProtectorView, TrustHasProtectorYesNoView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAProtectorController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         registrationsRepository: RegistrationsRepository,
                                         navigator: Navigator,
                                         standardActionSets: StandardActionSets,
                                         requiredAnswer: RequiredAnswerActionProvider,
                                         addAnotherFormProvider: AddAProtectorFormProvider,
                                         yesNoFormProvider: YesNoFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         addAnotherView: AddAProtectorView,
                                         yesNoView: TrustHasProtectorYesNoView,
                                         config: FrontendAppConfig,
                                         trustsStoreService: TrustsStoreService
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val addAnotherForm = addAnotherFormProvider()
  private val yesNoForm = yesNoFormProvider.withPrefix("trustHasProtectorYesNo")

  private def trustHasProtectorAnswer(draftId: String): RequiredAnswerAction[Boolean] =
    requiredAnswer(RequiredAnswer(TrustHasProtectorYesNoPage, routes.TrustHasProtectorYesNoController.onPageLoad(draftId)))

  private def heading(count: Int)(implicit mp: MessagesProvider): String = {
    count match {
      case x if x <= 1 => Messages("addAProtector.heading")
      case _ => Messages("addAProtector.count.heading", count)
    }
  }

  def onPageLoad(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).andThen(trustHasProtectorAnswer(draftId)) {
    implicit request =>

      val rows = new AddAProtectorViewHelper(request.userAnswers, draftId).rows

      if (rows.count > 0) {
        val maxedOut = request.userAnswers.protectors.maxedOutOptions.map(_.messageKey)

        if (maxedOut.size == 2) {logger.info(s"[Session ID: ${request.sessionId}] ${request.internalId} has maxed out protectors")}
        else {logger.info(s"[Session ID: ${request.sessionId}] ${request.internalId} has not maxed out protectors")}
        Ok(addAnotherView(addAnotherForm, draftId, rows.inProgress, rows.complete, heading(rows.count), maxedOut))
      } else {
        logger.info(s"[Session ID: ${request.sessionId}] ${request.internalId} has added no protectors")
        Ok(yesNoView(yesNoForm, draftId))
      }
  }

  def submitOne(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).andThen(trustHasProtectorAnswer(draftId)).async {
    implicit request =>

      yesNoForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(
            BadRequest(yesNoView(formWithErrors, draftId))
          )
        },
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAProtectorYesNoPage, value))
            _ <- registrationsRepository.set(updatedAnswers)
            _ <- setTaskStatus(draftId, if (value) InProgress else Completed)
          } yield Redirect(navigator.nextPage(AddAProtectorYesNoPage, draftId, updatedAnswers))
        }
      )
  }

  def submitAnother(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).andThen(trustHasProtectorAnswer(draftId)).async {
    implicit request =>

      addAnotherForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {

          val rows = new AddAProtectorViewHelper(request.userAnswers, draftId).rows

          Future.successful(BadRequest(
            addAnotherView(
              formWithErrors,
              draftId,
              rows.inProgress,
              rows.complete,
              heading(rows.count),
              request.userAnswers.protectors.maxedOutOptions.map(_.messageKey)
            )
          ))
        },
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAProtectorPage, value))
            _ <- registrationsRepository.set(updatedAnswers)
            _ <- setTaskStatus(draftId, value)
          } yield Redirect(navigator.nextPage(AddAProtectorPage, draftId, updatedAnswers))
        }
      )
  }

  def submitComplete(draftId: String): Action[AnyContent] = standardActionSets.identifiedUserWithData(draftId).andThen(trustHasProtectorAnswer(draftId)).async {
    implicit request =>

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAProtectorPage, NoComplete))
        _ <- registrationsRepository.set(updatedAnswers)
        _ <- setTaskStatus(draftId, Completed)
      } yield Redirect(Call(GET, config.registrationProgressUrl(draftId)))
  }

  private def setTaskStatus(draftId: String, selection: AddAProtector)
                           (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val status = if (selection == NoComplete) Completed else InProgress
    setTaskStatus(draftId, status)
  }

  private def setTaskStatus(draftId: String, taskStatus: TaskStatus)
                           (implicit hc: HeaderCarrier): Future[HttpResponse] = {
    trustsStoreService.updateTaskStatus(draftId, taskStatus)
  }

}
