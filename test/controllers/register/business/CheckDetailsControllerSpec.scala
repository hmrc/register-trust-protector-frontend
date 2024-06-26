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

package controllers.register.business

import base.SpecBase
import config.annotations.BusinessProtector
import models.Status.Completed
import models.UserAnswers
import models.register.pages.IndividualOrBusinessToAdd.Business
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.entitystatus.BusinessProtectorStatus
import pages.register.IndividualOrBusinessPage
import pages.register.business.NamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.print.BusinessProtectorPrintHelper
import views.html.register.business.CheckDetailsView

class CheckDetailsControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val index: Int = 0
  private val name = "Company"

  private lazy val checkDetailsRoute = routes.CheckDetailsController.onPageLoad(index, fakeDraftId).url

  override val emptyUserAnswers: UserAnswers = super.emptyUserAnswers
    .set(IndividualOrBusinessPage, Business).success.value
    .set(NamePage(index), name).success.value

  "CheckDetails Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, checkDetailsRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[CheckDetailsView]
      val printHelper = application.injector.instanceOf[BusinessProtectorPrintHelper]
      val answerSection = printHelper.checkDetailsSection(emptyUserAnswers, name, index, fakeDraftId)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(Seq(answerSection), index, fakeDraftId)(request, messages).toString

      application.stop()
    }

    "remove IndividualOrBusinessPage and redirect" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[Navigator].qualifiedWith(classOf[BusinessProtector]).toInstance(new FakeNavigator))
        .build()

      val request = FakeRequest(POST, checkDetailsRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(registrationsRepository).set(uaCaptor.capture)(any(), any())
      uaCaptor.getValue.get(BusinessProtectorStatus(index)).get mustBe Completed

      application.stop()
    }

  }
}
