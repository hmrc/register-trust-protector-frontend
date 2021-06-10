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

package navigation

import config.FrontendAppConfig
import controllers.register.business.{routes => brts}
import controllers.register.individual.{routes => irts}
import controllers.register.{routes => rts}
import models.ReadableUserAnswers
import models.register.pages.AddAProtector
import models.register.pages.IndividualOrBusinessToAdd.{Business, Individual}
import pages.register._
import pages.{Page, QuestionPage}
import play.api.libs.json.Reads
import play.api.mvc.Call
import sections.{BusinessProtectors, IndividualProtectors}
import viewmodels.addAnother.ProtectorViewModel

import javax.inject.Inject

class ProtectorNavigator @Inject()(config: FrontendAppConfig) extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call =
    route(draftId, config)(page)(userAnswers)

  private def route(draftId: String, config: FrontendAppConfig): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case AnswersPage => _ => rts.AddAProtectorController.onPageLoad(draftId)
    case AddAProtectorPage => addProtectorRoute(draftId, config)
    case AddAProtectorYesNoPage => addAProtectorYesNoRoute(draftId, config)
    case IndividualOrBusinessPage => individualOrBusinessRoute(draftId)
    case TrustHasProtectorYesNoPage => trustHasProtectorRoute(draftId)
  }

  private def protectorsCompletedRoute(draftId: String, config: FrontendAppConfig): Call = {
    Call("GET", config.registrationProgressUrl(draftId))
  }

  private def trustHasProtectorRoute(draftId: String)(userAnswers: ReadableUserAnswers) : Call =
    userAnswers.get(TrustHasProtectorYesNoPage) match {
      case Some(true) => controllers.register.routes.InfoController.onPageLoad(draftId)
      case Some(false) => protectorsCompletedRoute(draftId, config)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }

  private def individualOrBusinessRoute(draftId: String)(userAnswers: ReadableUserAnswers) : Call =
    userAnswers.get(IndividualOrBusinessPage) match {
      case Some(Individual) => routeToIndividualProtectorIndex(userAnswers, draftId)
      case Some(Business) => routeToBusinessProtectorIndex(userAnswers, draftId)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }

  private def routeToIndividualProtectorIndex(userAnswers: ReadableUserAnswers, draftId: String): Call = {
    routeToProtectorIndex(userAnswers, IndividualProtectors, irts.NameController.onPageLoad, draftId)
  }

  private def routeToBusinessProtectorIndex(userAnswers: ReadableUserAnswers, draftId: String): Call = {
    routeToProtectorIndex(userAnswers, BusinessProtectors, brts.NameController.onPageLoad, draftId)
  }

  private def routeToProtectorIndex[T <: ProtectorViewModel](userAnswers: ReadableUserAnswers,
                                                             page: QuestionPage[List[T]],
                                                             route: (Int, String) => Call,
                                                             draftId: String)
                                                            (implicit rds: Reads[T]): Call = {
    val protectors = userAnswers.get(page).getOrElse(List.empty)
    val index = protectors match {
      case Nil => 0
      case x if !x.last.isComplete => x.size - 1
      case x => x.size
    }
    route(index, draftId)
  }

  private def addProtectorRoute(draftId: String, config: FrontendAppConfig)(answers: ReadableUserAnswers): Call = {
    answers.get(AddAProtectorPage) match {
      case Some(AddAProtector.YesNow) =>
        (answers.get(IndividualProtectors).getOrElse(Nil).size, answers.get(BusinessProtectors).getOrElse(Nil).size) match {
          case (x, y) if x >= 25 => controllers.register.business.routes.NameController.onPageLoad(y, draftId)
          case (x, y) if y >= 25 => controllers.register.individual.routes.NameController.onPageLoad(x, draftId)
          case _ => controllers.register.routes.IndividualOrBusinessController.onPageLoad(draftId)
        }
      case Some(AddAProtector.YesLater) => protectorsCompletedRoute(draftId, config)
      case Some(AddAProtector.NoComplete) => protectorsCompletedRoute(draftId, config)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def addAProtectorYesNoRoute(draftId: String, config: FrontendAppConfig)(answers: ReadableUserAnswers): Call = {
    answers.get(AddAProtectorYesNoPage) match {
      case Some(true) =>
        controllers.register.routes.IndividualOrBusinessController.onPageLoad(draftId)
      case Some(false) => protectorsCompletedRoute(draftId, config)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

}
