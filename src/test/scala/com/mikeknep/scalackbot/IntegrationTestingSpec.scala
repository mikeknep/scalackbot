package com.mikeknep.scalackbot

import com.mikeknep.scalackbot.Handler.DefiniteOutcome
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class IntegrationTestingSpec extends FunSpec {
  describe("Integration testing") {

    object UpcaseHandler extends Handler[String] {
      override val helpText: String = "*upcase* the word"
      override def isRelevantMessage(data: SlackMessage): Boolean = data.text contains "upcase"
      override def perform(state: String, data: SlackMessage): DefiniteOutcome[String] = new DefiniteOutcome(state.toUpperCase, SlackResponse("the word was capitalized"))
    }

    object ReverseHandler extends Handler[String] {
      override val helpText: String = "*reverse* the word"
      override def isRelevantMessage(data: SlackMessage): Boolean = data.text contains "reverse"
      override def perform(state: String, data: SlackMessage): DefiniteOutcome[String] = new DefiniteOutcome(state.reverse, SlackResponse("the word was reversed"))
    }

    object DropFirstCharHandler extends Handler[String] {
      override val helpText: String = "*drop* the first character from the word"
      override def isRelevantMessage(data: SlackMessage): Boolean = data.text contains "drop"
      override def perform(state: String, data: SlackMessage): DefiniteOutcome[String] = new DefiniteOutcome(state.drop(1), SlackResponse("the first character was removed"))
    }

    object EraseWordHandler extends Handler[String] {
      override val helpText: String = "*erase* the word"
      override def isRelevantMessage(data: SlackMessage): Boolean = data.text contains "erase"
      override def perform(state: String, data: SlackMessage): DefiniteOutcome[String] = new DefiniteOutcome(state.drop(1), SlackResponse("the word was erased"))
    }

    it("returns a result to verify final state and sent messages") {
      val botName = "testBot"
      val handlers = List(UpcaseHandler, ReverseHandler, DropFirstCharHandler, EraseWordHandler)
      val originalWord = "Hello"
      val bot = Bot.builder().withName(botName).withToken("apiToken").withHandlers(handlers).withInitialState(originalWord).build

      val channel = "General"
      val user = "SomeUser"
      val messages = List(
        SlackMessage(s"$botName reverse the word please!", channel, user),
        SlackMessage(s"$botName upcase the word.", channel, user),
        SlackMessage("erase the word", channel, user),
        SlackMessage(s"$botName what can you do?", channel, user),
        SlackMessage(s"$botName drop the first character", channel, user)
      )

      val result = IntegrationTesting.run(bot, messages)

      result.appState should be("LLEH")
      result.slackClient.responseLog should be(List(
        "the word was reversed",
        "the word was capitalized",
        """Sorry, I didn't understand you. I can:
          |*upcase* the word
          |*reverse* the word
          |*drop* the first character from the word
          |*erase* the word""".stripMargin,
        "the first character was removed"
      ))
    }
  }
}
