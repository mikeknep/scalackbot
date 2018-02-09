package com.mikeknep.scalackbot

class IntegrationTestingSlackClient extends SlackClient {
  var responseLog: List[String] = List.empty

  override def respond(message: SlackMessage, response: SlackResponse): Unit = {
    responseLog = responseLog :+ response.text
  }
}

case class IntegrationTestResult[ClientState](appState: ClientState, slackClient: IntegrationTestingSlackClient)

object IntegrationTesting {
  def run[ClientState](bot: Bot[ClientState], messages: List[SlackMessage]): IntegrationTestResult[ClientState] = {
    val slackClient = new IntegrationTestingSlackClient
    val handlers = bot.handlers
    var appState = bot.initialState

    messages.foreach(message =>
      appState = Core.handleMessage(message, appState, handlers, bot.name, slackClient)
    )

    IntegrationTestResult(appState, slackClient)
  }
}
