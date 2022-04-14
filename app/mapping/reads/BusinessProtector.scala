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

package mapping.reads

import models.{IdentificationOrgType, InternationalAddress, UkAddress}
import play.api.libs.json.{Format, Json}

final case class BusinessProtector(name: String,
                                   utr: Option[String],
                                   ukAddress: Option[UkAddress],
                                   internationalAddress: Option[InternationalAddress],
                                   countryOfResidence: Option[String]) extends Protector {

  val identification: Option[IdentificationOrgType] = (utr, ukAddress, internationalAddress) match {
    case (None, None, None) => None
    case (Some(_),_ , _) => Some(IdentificationOrgType(utr, None))
    case _ => Some(IdentificationOrgType(None, address))
  }
}

object BusinessProtector {
  implicit val formats: Format[BusinessProtector] = Json.format[BusinessProtector]
}
