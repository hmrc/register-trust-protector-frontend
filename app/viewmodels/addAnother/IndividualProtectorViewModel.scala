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

import models.{FullName, Status}
import play.api.libs.json.{Reads, __}

case class IndividualProtectorViewModel(name: Option[FullName],
                                        status: Status) extends ProtectorViewModel {

  override def displayName: Option[String] = name.map(_.toString)
}

object IndividualProtectorViewModel {

  import play.api.libs.functional.syntax._

  implicit val reads : Reads[IndividualProtectorViewModel] = (
    (__ \ "name").readNullable[FullName] and
      (__ \ "status").readWithDefault[Status](Status.InProgress)
    )(IndividualProtectorViewModel.apply _)

}
