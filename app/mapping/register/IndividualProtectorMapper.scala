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

import mapping.reads.{IndividualProtector, IndividualProtectors}
import models.{IdentificationType, PassportOrIdCardDetails, PassportType, Protector}
import pages.QuestionPage

class IndividualProtectorMapper extends Mapper[Protector, IndividualProtector] {

  override def section: QuestionPage[List[IndividualProtector]] = IndividualProtectors

  override def protectorType(protector: IndividualProtector): Protector = Protector(
    name = protector.name,
    dateOfBirth = protector.dateOfBirth,
    identification = buildIdentification(protector),
    countryOfResidence = protector.countryOfResidence,
    nationality = protector.nationality,
    legallyIncapable = protector.legallyCapable.map(!_)
  )

  private def buildIdentification(protector: IndividualProtector): Option[IdentificationType] = {
    val nino = protector.nationalInsuranceNumber
    val address = protector.address
    val passport = protector.passportDetails
    val idCard = protector.idCardDetails
    (nino, address, passport, idCard) match {
      case (None, None, None, None) =>
        None
      case (Some(_), _, _, _) =>
        Some(IdentificationType(
          nino = nino,
          passport = None,
          address = None
        ))
      case (_, _, _, _) =>
        Some(IdentificationType(
          nino = None,
          passport = buildPassportOrIdCard(protector.passportDetails, protector.idCardDetails),
          address = address
        ))
    }
  }

  private def buildPassportOrIdCard(passport: Option[PassportOrIdCardDetails], idCardDetails: Option[PassportOrIdCardDetails]): Option[PassportType] =
    (passport, idCardDetails) match {
      case (Some(passport), _) => buildPassport(passport)
      case (_, Some(idCard)) => buildPassport(idCard)
      case _ => None
    }

  private def buildPassport(details: PassportOrIdCardDetails): Option[PassportType] =
    Some(PassportType(details.cardNumber, details.expiryDate, details.country))
}
