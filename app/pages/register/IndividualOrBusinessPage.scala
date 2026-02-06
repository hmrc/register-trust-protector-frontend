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

package pages.register

import models.UserAnswers
import models.register.pages.IndividualOrBusinessToAdd
import models.register.pages.IndividualOrBusinessToAdd._
import pages.QuestionPage
import play.api.libs.json.{JsPath, Reads}
import sections.{BusinessProtectors, IndividualProtectors, Protectors}
import viewmodels.addAnother.ProtectorViewModel

import scala.util.{Success, Try}

case object IndividualOrBusinessPage extends QuestionPage[IndividualOrBusinessToAdd] {

  override def path: JsPath = Protectors.path \ toString

  override def toString: String = "individualOrBusiness"

  override def cleanup(value: Option[IndividualOrBusinessToAdd], userAnswers: UserAnswers): Try[UserAnswers] = {

    def cleanupLastIfInProgress[T <: ProtectorViewModel](
      page: QuestionPage[List[T]]
    )(implicit rds: Reads[T]): Try[UserAnswers] = {
      val protectors = userAnswers.get(page).getOrElse(Nil)
      protectors match {
        case x if x.nonEmpty && !x.last.isComplete => userAnswers.deleteAtPath(page.path \ (protectors.size - 1))
        case _                                     => Success(userAnswers)
      }
    }

    value match {
      case Some(Individual) => cleanupLastIfInProgress(BusinessProtectors)
      case Some(Business)   => cleanupLastIfInProgress(IndividualProtectors)
      case None             => super.cleanup(value, userAnswers)
    }
  }

}
