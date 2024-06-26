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

package viewmodels.addAnother

import models.Status
import play.api.libs.json.Reads
import scala.language.implicitConversions

trait ProtectorViewModel {
  val status: Status
  def displayName: Option[String]

  def isComplete: Boolean = displayName.nonEmpty && (status == Status.Completed)
}

object ProtectorViewModel {

  implicit val reads : Reads[ProtectorViewModel] = {

    implicit class ReadsWithContravariantOr[A](a: Reads[A]) {

      def or[B >: A](b: Reads[B]): Reads[B] = {
        a.map[B](identity).orElse(b)
      }
    }

    implicit def convertToSupertype[A, B >: A](a: Reads[A]): Reads[B] =
      a.map(identity)

    IndividualProtectorViewModel.reads or
      BusinessProtectorViewModel.reads
  }
}
