package com.mikeknep.scalackbot

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class CoreSpec extends FunSpec {
  class SpyingHandler(shouldPerform: Boolean) extends Handler[Int] {
    var performWasCalled = false
    override val helpText = "I spy"
    override def isRelevantMessage(message: SlackMessage): Boolean = shouldPerform
    override def perform(state: Int, message: SlackMessage): Handler.DefiniteOutcome[Int] = {
      performWasCalled = true
      new Handler.DefiniteOutcome(state, SlackResponse("spying handler reply"))
    }
  }

  class IncrementingHandler extends Handler[Int] {
    val response = SlackResponse("inc handler response")
    override val helpText = "I increment the state"
    override def isRelevantMessage(message: SlackMessage): Boolean = true
    override def perform(state: Int, message: SlackMessage): Handler.DefiniteOutcome[Int] = {
      new Handler.DefiniteOutcome(state + 1, response)
    }
  }

  class SpyingSlackClient extends SlackClient {
    var respondWasCalled = false
    var respondArgs: (SlackMessage, SlackResponse) = _
    override def respond(message: SlackMessage, response: SlackResponse): Unit = {
      respondWasCalled = true
      respondArgs = (message, response)
    }
  }

  describe("The Scalackbot Runner") {
    val botName = "MyBot"
    val message = SlackMessage(text = s"Hello $botName", channel = "General", user = "John")
    val appState = 1
    val slackClient = new SpyingSlackClient
    val handler = new SpyingHandler(true)

    describe("getting the bot's attention") {
      it("does not perform any handlers when the message does not address the bot by name") {
        val messageToSomeoneElse = message.copy(text = "Hello Miles")
        Core.handleMessage(messageToSomeoneElse, appState, List(handler), botName, slackClient)

        handler.performWasCalled should be(false)
      }

      it("executes a relevant handler when the message addresses the bot by name") {
        val messageToBot = message.copy(text = s"Hello $botName")
        Core.handleMessage(messageToBot, appState, List(handler), botName, slackClient)

        handler.performWasCalled should be(true)
      }
    }

    describe("whether or not to execute a handler") {
      it("performs a handler when the message is relevant to the handler") {
        val handler = new SpyingHandler(true)
        Core.handleMessage(message, appState, List(handler), botName, slackClient)

        handler.performWasCalled should be(true)
      }

      it("does not perform the handler when the message is not relevant to the handler") {
        val handler = new SpyingHandler(false)
        Core.handleMessage(message, appState, List(handler), botName, slackClient)

        handler.performWasCalled should be(false)
      }

      it("only performs the first relevant handler") {
        val handler1 = new SpyingHandler(true)
        val handler2 = new SpyingHandler(true)
        Core.handleMessage(message, appState, List(handler1, handler2), botName, slackClient)

        handler1.performWasCalled should be(true)
        handler2.performWasCalled should be(false)
      }
    }

    describe("changing state") {
      it("returns a new, updated instance of the client state") {
        val incHandler = new IncrementingHandler
        val newState = Core.handleMessage(message, 1, List(incHandler), botName, slackClient)
        newState should be(2)
      }
    }

    describe("responding") {
      it("responds via the slack client with the response supplied by the handler that was called") {
        val handler1 = new SpyingHandler(false)
        val handler2 = new IncrementingHandler
        Core.handleMessage(message, appState, List(handler1, handler2), botName, slackClient)

        slackClient.respondWasCalled should be(true)
        slackClient.respondArgs should be((message, handler2.response))
      }

      it("responds with a formatted help message when no handlers were called") {
        val handler = new SpyingHandler(false)
        Core.handleMessage(message, appState, List(handler), botName, slackClient)

        slackClient.respondWasCalled should be(true)
        slackClient.respondArgs._2.text should include("Sorry")
        slackClient.respondArgs._2.text should include(handler.helpText)
      }
    }
  }
}
