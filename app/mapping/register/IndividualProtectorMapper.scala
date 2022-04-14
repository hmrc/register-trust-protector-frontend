/*
 * Copyright 2022 HM Revenue & Customs
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

package mapping.register

import mapping.reads.IndividualProtector
import models.Protector
import models.YesNoDontKnow.{DontKnow, No, Yes}
import play.api.libs.json.JsPath
import sections.IndividualProtectors

class IndividualProtectorMapper extends Mapper[Protector, IndividualProtector] {

  override def jsPath: JsPath = IndividualProtectors.path

  override def protectorType(protector: IndividualProtector): Protector = Protector(
    name = protector.name,
    dateOfBirth = protector.dateOfBirth,
    identification = protector.identification,
    countryOfResidence = protector.countryOfResidence,
    nationality = protector.nationality,
    legallyIncapable = {
      protector.legallyCapable.flatMap {
        case Yes => Some(false)
        case No => Some(true)
        case DontKnow => None
      }
    }
  )
}
