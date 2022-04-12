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

package views.register

import forms.AddAProtectorFormProvider
import models.register.pages.{AddAProtector, IndividualOrBusinessToAdd}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.AddRow
import views.behaviours.{OptionsViewBehaviours, TabularDataViewBehaviours}
import views.html.register.AddAProtectorView

class AddAProtectorViewSpec extends OptionsViewBehaviours with TabularDataViewBehaviours {

  val fakeRow: AddRow = AddRow("Name", "Type", "#", "#")

  val messageKeyPrefix = "addAProtector"

  val form = new AddAProtectorFormProvider()()

  val view: AddAProtectorView = viewFor[AddAProtectorView](Some(emptyUserAnswers))

  def applyView(form: Form[_]): HtmlFormat.Appendable =
    view.apply(form, fakeDraftId, Nil, Nil, "Add a protector", Nil)(fakeRequest, messages)

  def applyView(form: Form[_], inProgressProtectors: Seq[AddRow], completeProtectors: Seq[AddRow], count: Int, maxedOut: List[String]): HtmlFormat.Appendable = {
    val title = if (count > 1) s"You have added $count protectors" else "You have added 1 protector"
    view.apply(form, fakeDraftId, inProgressProtectors, completeProtectors, title, maxedOut)(fakeRequest, messages)
  }

  "AddAProtectorView" when {

    "there is no protector data" must {

      behave like normalPage(applyView(form), messageKeyPrefix)

      behave like pageWithBackLink(applyView(form))

      behave like pageWithNoTabularData(applyView(form))

      behave like pageWithOptions(form, applyView, AddAProtector.options)
    }

    "there is data in progress" must {

      val inProgressProtectors = List.fill(2)(fakeRow)

      val viewWithData = applyView(form, inProgressProtectors, Nil, 2, Nil)

      behave like dynamicTitlePage(viewWithData, "addAProtector.count", "2")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithInProgressTabularData(viewWithData, inProgressProtectors)

      behave like pageWithOptions(form, applyView, AddAProtector.options)
    }

    "there is complete data" must {

      val completeProtectors = List.fill(2)(fakeRow)

      val viewWithData = applyView(form, Nil, completeProtectors, 2, Nil)

      behave like dynamicTitlePage(viewWithData, "addAProtector.count", "2")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithCompleteTabularData(viewWithData, completeProtectors)

      behave like pageWithOptions(form, applyView, AddAProtector.options)
    }

    "there is both in progress and complete data" must {

      val inProgressProtectors = List.fill(2)(fakeRow)
      val completeProtectors = List.fill(2)(fakeRow)

      val viewWithData = applyView(form, inProgressProtectors, completeProtectors, 4, Nil)

      behave like dynamicTitlePage(viewWithData, "addAProtector.count", "4")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithTabularData(viewWithData, inProgressProtectors, completeProtectors)

      behave like pageWithOptions(form, applyView, AddAProtector.options)
    }

    "all protectors are maxed out" must {

      val inProgressProtectors = List.fill(25)(fakeRow)
      val completeProtectors = List.fill(25)(fakeRow)

      val viewWithData = applyView(
        form = form,
        inProgressProtectors = inProgressProtectors,
        completeProtectors = completeProtectors,
        count = 50,
        maxedOut = List(IndividualOrBusinessToAdd.Individual.toString, IndividualOrBusinessToAdd.Business.toString)
      )

      behave like dynamicTitlePage(viewWithData, "addAProtector.count", "50")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithTabularData(viewWithData, inProgressProtectors, completeProtectors)

      "shows no radios and shows content for maxed protectors" in {
        val doc = asDocument(viewWithData)
        AddAProtector.options.foreach(x => assertNotRenderedById(doc, x.id))
        assertContainsText(doc, messages("addAProtector.maxedOut.all"))
        assertContainsText(doc, messages("addAProtector.maxedOut.all.paragraph"))
      }
    }

    "individual protectors are maxed out" must {

      val inProgressProtectors = Nil
      val completeProtectors = List.fill(25)(fakeRow)

      val viewWithData = applyView(
        form = form,
        inProgressProtectors = inProgressProtectors,
        completeProtectors = completeProtectors,
        count = 25,
        maxedOut = List(IndividualOrBusinessToAdd.Individual.toString)
      )

      behave like dynamicTitlePage(viewWithData, "addAProtector.count", "25")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithTabularData(viewWithData, inProgressProtectors, completeProtectors)

      "shows radios and shows content for maxed individual protectors" in {
        val doc = asDocument(viewWithData)
        AddAProtector.options.foreach(x => assertRenderedById(doc, x.id))
        assertContainsText(doc, messages("addAProtector.maxedOut", "individual"))
        assertContainsText(doc, messages("addAProtector.maxedOut.paragraph"))
      }
    }

    "business protectors are maxed out" must {

      val inProgressProtectors = Nil
      val completeProtectors = List.fill(25)(fakeRow)

      val viewWithData = applyView(
        form = form,
        inProgressProtectors = inProgressProtectors,
        completeProtectors = completeProtectors,
        count = 25,
        maxedOut = List(IndividualOrBusinessToAdd.Business.toString)
      )

      behave like dynamicTitlePage(viewWithData, "addAProtector.count", "25")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithTabularData(viewWithData, inProgressProtectors, completeProtectors)

      "shows radios and shows content for maxed business protectors" in {
        val doc = asDocument(viewWithData)
        AddAProtector.options.foreach(x => assertRenderedById(doc, x.id))
        assertContainsText(doc, messages("addAProtector.maxedOut", "business"))
        assertContainsText(doc, messages("addAProtector.maxedOut.paragraph"))
      }
    }
  }

}
