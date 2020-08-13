/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject
import models._
import play.api.i18n.Messages
import play.api.libs.json.Json
import viewmodels.{AnswerRow, AnswerSection}

class SubmissionSetFactory @Inject()() {

  def createFrom(userAnswers: UserAnswers)(implicit messages: Messages): RegistrationSubmission.DataSet = {
    val status = Some(Status.InProgress)
    answerSectionsIfCompleted(userAnswers, status)

    RegistrationSubmission.DataSet(
      Json.toJson(userAnswers),
      status,
      mappedDataIfCompleted(userAnswers, status),
      answerSectionsIfCompleted(userAnswers, status)
    )
  }

  private def mappedDataIfCompleted(userAnswers: UserAnswers, status: Option[Status]) = {
//    if (status.contains(Status.Completed)) {
//      trusteesMapper.build(userAnswers) match {
//        case Some(assets) => List(RegistrationSubmission.MappedPiece("trust/entities/beneficiary", Json.toJson(assets)))
//        case _ => List.empty
//      }
//    } else {
      List.empty
//    }
  }

  def answerSectionsIfCompleted(userAnswers: UserAnswers, status: Option[Status])
                               (implicit messages: Messages): List[RegistrationSubmission.AnswerSection] = {

//    if (status.contains(Status.Completed)) {
//
//      val individualBeneficiariesHelper = new IndividualBeneficiaryAnswersHelper(countryOptions)(userAnswers, userAnswers.draftId, false)
//      val classOfBeneficiariesHelper = new ClassOfBeneficiariesAnswersHelper(countryOptions)(userAnswers, userAnswers.draftId, false)
//      val charityBeneficiariesHelper = new CharityBeneficiaryAnswersHelper(countryOptions)(userAnswers, userAnswers.draftId, false)
//      val trustBeneficiariesHelper = new TrustBeneficiaryAnswersHelper(countryOptions)(userAnswers, userAnswers.draftId, false)
//
//      val entitySections = List(
//        individualBeneficiariesHelper.individualBeneficiaries,
//        classOfBeneficiariesHelper.classOfBeneficiaries,
//        charityBeneficiariesHelper.charityBeneficiaries,
//        trustBeneficiariesHelper.trustBeneficiaries,
//        companyBeneficiaryAnswersHelper.companyBeneficiaries(userAnswers, canEdit = false),
//        largeBeneficiaryAnswersHelper.employmentRelatedBeneficiaries(userAnswers, canEdit = false),
//        otherBeneficiaryAnswersHelper.otherBeneficiaries(userAnswers, canEdit = false)
//      ).flatten.flatten
//
//      val updatedFirstSection = AnswerSection(
//        entitySections.head.headingKey,
//        entitySections.head.rows,
//        Some(Messages("answerPage.section.beneficiaries.heading"))
//      )
//
//      val updatedSections = updatedFirstSection :: entitySections.tail
//
//      updatedSections.map(convertForSubmission)
//
//    } else {
      List.empty
//    }
  }

  private def convertForSubmission(row: AnswerRow): RegistrationSubmission.AnswerRow = {
    RegistrationSubmission.AnswerRow(row.label, row.answer.toString, row.labelArg)
  }

  private def convertForSubmission(section: AnswerSection): RegistrationSubmission.AnswerSection = {
    RegistrationSubmission.AnswerSection(section.headingKey, section.rows.map(convertForSubmission), section.sectionKey)
  }
}
