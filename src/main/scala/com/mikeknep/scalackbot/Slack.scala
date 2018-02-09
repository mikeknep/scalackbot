package com.mikeknep.scalackbot

case class SlackMessage(text: String, channel: String, user: String)

case class SlackResponse(text: String)

trait SlackClient {
  def respond(message: SlackMessage, response: SlackResponse): Unit
}
