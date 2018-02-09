package com.mikeknep.scalackbot

trait Handler[ClientState] {
  val helpText: String
  def isRelevantMessage(data: SlackMessage): Boolean
  def perform(state: ClientState, data: SlackMessage): Handler.DefiniteOutcome[ClientState]
}

object Handler {
  case class Outcome[ClientState, M[_]](state: ClientState, response: M[SlackResponse])

  type Identity[A] = A

  type DefiniteOutcome[ClientState] = Outcome[ClientState, Identity]
  type OptionalOutcome[ClientState] = Outcome[ClientState, Option]
}
