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

import base.SpecBase
import controllers.routes
import models.Status.{Completed, InProgress}
import models.{UkAddress, UserAnswers}
import viewmodels.{AddRow, AddToRows}
import controllers.register.business.{routes => brts}
import pages.entitystatus.BusinessProtectorStatus
import pages.register.business._


class AddAProtectorViewHelperSpec extends SpecBase {

  private lazy val featureUnavailableUrl: String = routes.FeatureNotAvailableController.onPageLoad().url

  private def changeInProgressProtectorRoute(index: Int): String = brts.NameController.onPageLoad(index, draftId).url
  private def changeCompleteProtectorRoute(index: Int): String = brts.CheckDetailsController.onPageLoad(index, draftId).url
  private def removeProtectorRoute(index: Int): String = brts.CheckDetailsController.onPageLoad(index, draftId).url

  "Add a protector view helper" when {

    def helper(userAnswers: UserAnswers) = new AddAProtectorViewHelper(userAnswers, fakeDraftId)

    "business protector" must {

      val name: String = "Business"
      val label: String = "Business protector"

      "render a complete" in {

        val index: Int = 0

        val userAnswers = emptyUserAnswers
          .set(NamePage(index), name).success.value
          .set(UtrYesNoPage(index), false).success.value
          .set(AddressYesNoPage(index), true).success.value
          .set(AddressUkYesNoPage(index), true).success.value
          .set(UkAddressPage(index), UkAddress("line1", "line2", None, None, "NE99 1NE")).success.value
          .set(BusinessProtectorStatus(index), Completed).success.value

        helper(userAnswers).rows mustEqual AddToRows(
          inProgress = Nil,
          complete = List(
            AddRow(
              name = name,
              typeLabel = label,
              changeUrl = changeCompleteProtectorRoute(index),
              removeUrl = removeProtectorRoute(index)
            )
          )
        )
      }

      "render an in progress" when {

        "it has a name" in {

          val index: Int = 0

          val userAnswers = emptyUserAnswers
            .set(NamePage(index), name).success.value
            .set(UtrYesNoPage(index), false).success.value
            .set(BusinessProtectorStatus(index), InProgress).success.value

          helper(userAnswers).rows mustEqual AddToRows(
            inProgress = List(
              AddRow(
                name = name,
                typeLabel = label,
                changeUrl = changeInProgressProtectorRoute(index),
                removeUrl = removeProtectorRoute(index)
              )
            ),
            complete = Nil
          )
        }
      }

      "render multiple business protectors" in {

        val name1 = "Name 1"
        val name2 = "Name 2"
        val name3 = "Name 3"

        val userAnswers = emptyUserAnswers
          .set(NamePage(0), name1).success.value
          .set(UtrYesNoPage(0), true).success.value
          .set(UtrPage(0), "1234567890").success.value
          .set(BusinessProtectorStatus(0), Completed).success.value

          .set(NamePage(1), name2).success.value
          .set(UtrYesNoPage(1), true).success.value
          .set(UtrPage(1), "1234567890").success.value
          .set(BusinessProtectorStatus(1), Completed).success.value

          .set(NamePage(2), name3).success.value
          .set(UtrYesNoPage(2), false).success.value
          .set(BusinessProtectorStatus(2), InProgress).success.value

        helper(userAnswers).rows mustEqual AddToRows(
          inProgress = List(
            AddRow(
              name = name3,
              typeLabel = label,
              changeUrl = changeInProgressProtectorRoute(2),
              removeUrl = removeProtectorRoute(2)
            )
          ),
          complete = List(
            AddRow(
              name = name1,
              typeLabel = label,
              changeUrl = changeCompleteProtectorRoute(0),
              removeUrl = removeProtectorRoute(0)
            ),
            AddRow(
              name = name2,
              typeLabel = label,
              changeUrl = changeCompleteProtectorRoute(1),
              removeUrl = removeProtectorRoute(1)
            )
          )
        )
      }
    }
  }

}
