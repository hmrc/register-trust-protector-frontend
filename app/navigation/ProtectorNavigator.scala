/*
 * Copyright 2023 HM Revenue & Customs
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
import models.register.pages.IndividualOrBusinessToAdd.{Business, Individual}
import models.register.pages.{AddAProtector, IndividualOrBusinessToAdd}
import models.{Protectors, ReadableUserAnswers}
import pages.Page
import pages.register._
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.Constants.MAX
import viewmodels.addAnother.{BusinessProtectorViewModel, IndividualProtectorViewModel, ProtectorViewModel}

import javax.inject.Inject

class ProtectorNavigator @Inject()(config: FrontendAppConfig) extends Navigator {

  override def nextPage(page: Page, draftId: String, userAnswers: ReadableUserAnswers): Call =
    route(draftId)(page)(userAnswers)

  private def route(draftId: String): PartialFunction[Page, ReadableUserAnswers => Call] = {
    case AnswersPage => _ => rts.AddAProtectorController.onPageLoad(draftId)
    case AddAProtectorPage => addProtectorRoute(draftId)
    case AddAProtectorYesNoPage => addAProtectorYesNoRoute(draftId)
    case IndividualOrBusinessPage => individualOrBusinessRoute(draftId)
    case TrustHasProtectorYesNoPage => trustHasProtectorRoute(draftId)
  }

  private def protectorsCompletedRoute(draftId: String): Call = {
    Call(GET, config.registrationProgressUrl(draftId))
  }

  private def trustHasProtectorRoute(draftId: String)(userAnswers: ReadableUserAnswers): Call =
    userAnswers.get(TrustHasProtectorYesNoPage) match {
      case Some(true) => controllers.register.routes.InfoController.onPageLoad(draftId)
      case Some(false) => protectorsCompletedRoute(draftId)
      case _ => controllers.routes.SessionExpiredController.onPageLoad
    }

  private def individualOrBusinessRoute(draftId: String)(userAnswers: ReadableUserAnswers): Call =
    userAnswers.get(IndividualOrBusinessPage) match {
      case Some(individualOrBusiness) => ProtectorNavigator.addProtectorNowRoute(individualOrBusiness, userAnswers.protectors, draftId)
      case _ => controllers.routes.SessionExpiredController.onPageLoad
    }

  private def addProtectorRoute(draftId: String)(answers: ReadableUserAnswers): Call = {
    answers.get(AddAProtectorPage) match {
      case Some(AddAProtector.YesNow) => ProtectorNavigator.addProtectorRoute(answers.protectors, draftId)
      case Some(_) => protectorsCompletedRoute(draftId)
      case _ => controllers.routes.SessionExpiredController.onPageLoad
    }
  }

  private def addAProtectorYesNoRoute(draftId: String)(answers: ReadableUserAnswers): Call = {
    answers.get(AddAProtectorYesNoPage) match {
      case Some(true) =>
        controllers.register.routes.IndividualOrBusinessController.onPageLoad(draftId)
      case Some(false) => protectorsCompletedRoute(draftId)
      case _ => controllers.routes.SessionExpiredController.onPageLoad
    }
  }

}

object ProtectorNavigator {

  def addProtectorRoute(protectors: Protectors, draftId: String): Call = {
    val routes: List[(List[ProtectorViewModel], Call)] = List(
      (protectors.individuals, addProtectorNowRoute(Individual, protectors, draftId)),
      (protectors.businesses, addProtectorNowRoute(Business, protectors, draftId))
    )

    routes.filter(_._1.size < MAX) match {
      case (_, x) :: Nil => x
      case _ => controllers.register.routes.IndividualOrBusinessController.onPageLoad(draftId)
    }
  }

  def addProtectorNowRoute(`type`: IndividualOrBusinessToAdd, protectors: Protectors, draftId: String): Call = {
    `type` match {
      case Individual => routeToIndividualProtectorIndex(protectors.individuals, draftId)
      case Business => routeToBusinessProtectorIndex(protectors.businesses, draftId)
    }
  }

  private def routeToIndividualProtectorIndex(protectors: List[IndividualProtectorViewModel], draftId: String): Call = {
    routeToProtectorIndex(protectors, irts.NameController.onPageLoad, draftId)
  }

  private def routeToBusinessProtectorIndex(protectors: List[BusinessProtectorViewModel], draftId: String): Call = {
    routeToProtectorIndex(protectors, brts.NameController.onPageLoad, draftId)
  }

  private def routeToProtectorIndex[T <: ProtectorViewModel](protectors: List[T],
                                                             route: (Int, String) => Call,
                                                             draftId: String): Call = {
    route(protectors.size, draftId)
  }
}
