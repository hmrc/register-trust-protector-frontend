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

package repositories

import mapping.register.ProtectorsMapper
import models._
import pages.register.TrustHasProtectorYesNoPage
import play.api.i18n.Messages
import play.api.libs.json.{JsNull, JsValue, Json}
import utils.RegistrationProgress
import utils.answers.{BusinessProtectorAnswersHelper, IndividualProtectorAnswersHelper}
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class SubmissionSetFactory @Inject() (
  registrationProgress: RegistrationProgress,
  protectorsMapper: ProtectorsMapper,
  individualProtectorAnswersHelper: IndividualProtectorAnswersHelper,
  businessProtectorAnswerHelper: BusinessProtectorAnswersHelper
) {

  def createFrom(userAnswers: UserAnswers)(implicit messages: Messages): RegistrationSubmission.DataSet =
    RegistrationSubmission.DataSet(
      data = Json.toJson(userAnswers),
      registrationPieces = mappedData(userAnswers),
      answerSections = answerSections(userAnswers)
    )

  private def mappedPieces(protectorsJson: JsValue) =
    List(RegistrationSubmission.MappedPiece("trust/entities/protectors", protectorsJson))

  private def mappedData(userAnswers: UserAnswers): List[RegistrationSubmission.MappedPiece] =
    protectorsMapper.build(userAnswers) match {
      case Some(protectors) => mappedPieces(Json.toJson(protectors))
      case _                => mappedPieces(JsNull)
    }

  def answerSections(
    userAnswers: UserAnswers
  )(implicit messages: Messages): List[RegistrationSubmission.AnswerSection] = {

    val trustHasProtectorYesNo = userAnswers.get(TrustHasProtectorYesNoPage).contains(true)

    val entitySections = List(
      individualProtectorAnswersHelper.protectors(userAnswers),
      businessProtectorAnswerHelper.protectors(userAnswers)
    ).flatten.flatten

    if (entitySections.nonEmpty && trustHasProtectorYesNo) {

      val updatedFirstSection = entitySections.head.copy(sectionKey = Some("answerPage.section.protectors.heading"))

      val updatedSections = updatedFirstSection :: entitySections.tail

      updatedSections.map(convertForSubmission)

    } else {
      List.empty
    }
  }

  private def convertForSubmission(row: AnswerRow): RegistrationSubmission.AnswerRow =
    RegistrationSubmission.AnswerRow(
      label = row.label,
      answer = row.answer.toString,
      labelArg = row.labelArg
    )

  private def convertForSubmission(section: AnswerSection): RegistrationSubmission.AnswerSection =
    RegistrationSubmission.AnswerSection(
      headingKey = section.headingKey,
      rows = section.rows.map(convertForSubmission),
      sectionKey = section.sectionKey,
      headingArgs = section.headingArgs.map(_.toString)
    )

}
