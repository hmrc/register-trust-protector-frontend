/*
 * Copyright 2021 HM Revenue & Customs
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

import mapping.reads.{BusinessProtector, BusinessProtectors}
import models.{IdentificationOrgType, ProtectorCompany}
import pages.QuestionPage

class BusinessProtectorMapper extends Mapper[ProtectorCompany, BusinessProtector] {

  override def section: QuestionPage[List[BusinessProtector]] = BusinessProtectors

  override def protectorType(protector: BusinessProtector): ProtectorCompany = ProtectorCompany(
    name = protector.name,
    identification = buildIdentification(protector),
    countryOfResidence = protector.countryOfResidence
  )

  private def buildIdentification(protector: BusinessProtector): Option[IdentificationOrgType] = {
    (protector.utr, protector.ukAddress, protector.internationalAddress) match {
      case (None, None, None) => None
      case (Some(utr),_ , _) => Some(IdentificationOrgType(Some(utr), None))
      case _ => Some(IdentificationOrgType(None, protector.address))
    }
  }
}
