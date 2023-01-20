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

package utils

import models.Status._
import models.register.pages.AddAProtector
import models.{ReadableUserAnswers, Status}
import pages.QuestionPage
import pages.register.{AddAProtectorPage, TrustHasProtectorYesNoPage}
import play.api.libs.json.Reads
import sections.{BusinessProtectors, IndividualProtectors}
import viewmodels.addAnother._

class RegistrationProgress {

  def protectorsStatus(userAnswers: ReadableUserAnswers): Option[Status] = {

    if (!userAnswers.isAnyProtectorAdded) {
      userAnswers.get(TrustHasProtectorYesNoPage) map {
        case true => InProgress
        case false => Completed
      }
    } else {
      val statusList: List[IsComplete] = List(
        AddingProtectorsIsComplete,
        BusinessProtectorsAreComplete,
        IndividualProtectorsAreComplete
      )

      val isComplete = statusList.forall(_.apply(userAnswers))
      Some(if (isComplete) Completed else InProgress)
    }
  }

  sealed trait IsComplete {
    def apply(userAnswers: ReadableUserAnswers): Boolean
  }

  sealed class ListIsComplete[T <: ProtectorViewModel](section: QuestionPage[List[T]])
                                                      (implicit reads: Reads[T]) extends IsComplete {

    override def apply(userAnswers: ReadableUserAnswers): Boolean = {
      userAnswers.get(section) match {
        case Some(protectors) => !protectors.exists(_.status == InProgress)
        case _ => true
      }
    }
  }

  private object AddingProtectorsIsComplete extends IsComplete {
    override def apply(userAnswers: ReadableUserAnswers): Boolean =
      userAnswers.get(AddAProtectorPage).contains(AddAProtector.NoComplete)
  }

  private object BusinessProtectorsAreComplete extends ListIsComplete(BusinessProtectors)
  private object IndividualProtectorsAreComplete extends ListIsComplete(IndividualProtectors)
}
