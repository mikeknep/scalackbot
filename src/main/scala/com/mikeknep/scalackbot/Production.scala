package com.mikeknep.scalackbot

import slack.models.Message
import slack.rtm.SlackRtmClient
import akka.actor.ActorSystem

case class ProductionSlackClient(botToken: String) extends SlackClient {
  override def respond(message: SlackMessage, response: SlackResponse): Unit = {
    rtmClient.sendMessage(message.channel, response.text)
  }

  def onMessage(f: (Message) => Unit): Unit = {
    rtmClient.onMessage(f)
  }

  private implicit val system: ActorSystem = ActorSystem("slack")
  private val rtmClient = SlackRtmClient(botToken)
}

object Production {
  def run[ClientState](bot: Bot[ClientState]): Unit = {
    val slackClient = ProductionSlackClient(bot.token)
    val handlers = bot.handlers
    var appState = bot.initialState

    slackClient.onMessage(message =>
      appState = Core.handleMessage(wrapSlackMessage(message), appState, handlers, bot.name, slackClient)
    )
  }

  private def wrapSlackMessage(message: Message): SlackMessage = {
    SlackMessage(text = message.text, channel = message.channel, user = message.user)
  }
}
