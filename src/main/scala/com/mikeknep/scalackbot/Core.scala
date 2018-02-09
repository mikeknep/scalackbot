package com.mikeknep.scalackbot

object Core {

  def handleMessage[ClientState]( message: SlackMessage,
                                  appState: ClientState,
                                  handlers: List[Handler[ClientState]],
                                  botName: String,
                                  slackClient: SlackClient
                                ): ClientState = {
    if (isMessageToBot(message, botName)) {
      val handlersResult = executeHandlers(appState, message, handlers)
      slackClient.respond(message, handlersResult.response)
      handlersResult.state
    } else {
      appState
    }
  }

  private def isMessageToBot(message: SlackMessage, botName: String): Boolean = {
    message.text contains botName
  }

  private def executeHandlers[ClientState]( appState: ClientState,
                                            message: SlackMessage,
                                            handlers: List[Handler[ClientState]]
                                          ): Handler.DefiniteOutcome[ClientState] = {
    val initial = new Handler.OptionalOutcome(appState, None)
    val outcome = handlers.foldLeft(initial) (executeHandler(message))
    val response = outcome.response.getOrElse(buildHelpResponse(handlers))
    new Handler.DefiniteOutcome(outcome.state, response)
  }

  private def executeHandler[ClientState](message: SlackMessage)
                                         (outcome: Handler.OptionalOutcome[ClientState], handler: Handler[ClientState]): Handler.OptionalOutcome[ClientState] = {
    outcome.response match {
      case Some(_) => outcome
      case None => maybePerform(message, outcome.state, handler)
    }
  }

  private def maybePerform[ClientState](message: SlackMessage, appState: ClientState, handler: Handler[ClientState]): Handler.OptionalOutcome[ClientState] = {
    if (handler.isRelevantMessage(message)) {
      val outcome: Handler.DefiniteOutcome[ClientState] = handler.perform(appState, message)
      new Handler.OptionalOutcome(outcome.state, Some(outcome.response))
    } else {
      new Handler.OptionalOutcome(appState, None)
    }
  }

  def buildHelpResponse[ClientState](handlers: List[Handler[ClientState]]): SlackResponse = {
    val response = "Sorry, I didn't understand you. I can:\n" + handlers.map(_.helpText).mkString("\n")
    SlackResponse(response)
  }
}
