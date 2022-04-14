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

import mapping.register.IdentificationMapper.buildPassport
import models.{FullName, IdentificationType, InternationalAddress, PassportOrIdCardDetails, UkAddress, YesNoDontKnow}
import play.api.libs.json._
import play.api.libs.functional.syntax._

import java.time.LocalDate

final case class IndividualProtector(name: FullName,
                                     dateOfBirth: Option[LocalDate],
                                     nationalInsuranceNumber: Option[String],
                                     ukAddress: Option[UkAddress],
                                     internationalAddress: Option[InternationalAddress],
                                     passportDetails: Option[PassportOrIdCardDetails],
                                     idCardDetails: Option[PassportOrIdCardDetails],
                                     countryOfResidence: Option[String],
                                     nationality: Option[String],
                                     legallyCapable: Option[YesNoDontKnow]) extends Protector {

  val identification: Option[IdentificationType] = (nationalInsuranceNumber, address, passportDetails, idCardDetails) match {
    case (None, None, None, None) => None
    case (Some(_), _, _, _) => Some(IdentificationType(nationalInsuranceNumber, None, None))
    case _ => Some(IdentificationType(None, buildValue(passportDetails, idCardDetails)(buildPassport), address))
  }
}

object IndividualProtector {
  implicit val reads: Reads[IndividualProtector] = (
    (__ \ "name").read[FullName] and
      (__ \ "dateOfBirth").readNullable[LocalDate] and
      (__ \ "nationalInsuranceNumber").readNullable[String] and
      (__ \ "ukAddress").readNullable[UkAddress] and
      (__ \ "internationalAddress").readNullable[InternationalAddress] and
      (__ \ "passportDetails").readNullable[PassportOrIdCardDetails] and
      (__ \ "idCardDetails").readNullable[PassportOrIdCardDetails] and
      (__ \ "countryOfResidence").readNullable[String] and
      (__ \ "nationality").readNullable[String] and
      readMentalCapacity
    ) (IndividualProtector.apply _)

  implicit val writes: Writes[IndividualProtector] = Json.writes[IndividualProtector]

  def readMentalCapacity: Reads[Option[YesNoDontKnow]] =
    (__ \ 'legallyCapable).readNullable[Boolean].flatMap[Option[YesNoDontKnow]] { x: Option[Boolean] =>
      Reads(_ => JsSuccess(YesNoDontKnow.fromBoolean(x)))
    }.orElse {
      (__ \ 'legallyCapable).readNullable[YesNoDontKnow]
    }

}