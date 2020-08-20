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

package utils

import models.UserAnswers
import play.api.i18n.Messages
import viewmodels.{AddRow, AddToRows}
import viewmodels.addAnother.BusinessProtectorViewModel
import controllers.register.business.{routes => businessRts}
import sections.BusinessProtectors

class AddAProtectorViewHelper(userAnswers: UserAnswers, draftId : String)(implicit messages: Messages) {

  private case class InProgressComplete(inProgress : List[AddRow], complete: List[AddRow])

  private def parseName(name : Option[String]) : String = {
    val defaultValue = messages("entities.no.name.added")
    name.getOrElse(defaultValue)
  }

  private def parseBusinessProtector(businessProtector : (BusinessProtectorViewModel, Int)) : AddRow = {

    val vm = businessProtector._1
    val index = businessProtector._2

    AddRow(
      name = parseName(vm.name.map(_.toString)),
      typeLabel = messages("entities.protector.business"),
      changeUrl = if (vm.isComplete) {
        businessRts.CheckDetailsController.onPageLoad(index, draftId).url
      } else {
        businessRts.NameController.onPageLoad(index, draftId).url
      },
      removeUrl = businessRts.CheckDetailsController.onPageLoad(index, draftId).url // TODO businessRts.RemoveIndividualBeneficiaryController.onPageLoad(index, draftId).url
    )
  }


  private def businessProtectors = {
    val businessProtectors = userAnswers.get(BusinessProtectors).toList.flatten.zipWithIndex
    val businessProtectorsComplete = businessProtectors.filter(_._1.isComplete).map(parseBusinessProtector)
    val businessProtectorsInProgress = businessProtectors.filterNot(_._1.isComplete).map(parseBusinessProtector)

    InProgressComplete(inProgress = businessProtectorsInProgress, complete = businessProtectorsComplete)
  }

  def rows : AddToRows =
    AddToRows(
      inProgress = businessProtectors.inProgress,
      complete = businessProtectors.complete
    )

}
