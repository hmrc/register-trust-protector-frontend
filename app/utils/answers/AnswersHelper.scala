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

package utils.answers

import models.UserAnswers
import pages.QuestionPage
import play.api.i18n.Messages
import play.api.libs.json.Reads
import utils.print.PrintHelper
import viewmodels.AnswerSection
import viewmodels.addAnother.ProtectorViewModel

abstract class AnswersHelper[T <: ProtectorViewModel](printHelper: PrintHelper) {

  val protectorType: QuestionPage[List[T]]

  def protectors(userAnswers: UserAnswers)(implicit messages: Messages, rds: Reads[T]): Option[Seq[AnswerSection]] =
    for {
      protectors <- userAnswers.get(protectorType)
      indexed     = protectors.zipWithIndex
    } yield indexed.map { case (protector, index) =>
      printHelper.printSection(userAnswers, protector.displayName, index, userAnswers.draftId)
    }

}
