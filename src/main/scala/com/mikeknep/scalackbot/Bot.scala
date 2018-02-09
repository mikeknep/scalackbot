package com.mikeknep.scalackbot

case class Bot[ClientState](
  token: String,
  initialState: ClientState,
  handlers: List[Handler[ClientState]],
  name: String
)

object Bot {
  sealed trait Config
  protected object Config {
    sealed trait None extends Config
    sealed trait Token extends Config
    sealed trait InitialState extends Config
    sealed trait Handlers extends Config
    sealed trait Name extends Config
    type All = None with Token with InitialState with Handlers with Name
  }

  def builder[ClientState](): Builder[ClientState, Config.None] = {
    Builder[ClientState, Config.None](None, None, None, None)
  }

  case class Builder[ClientState, Config <: Bot.Config](
    token: Option[String],
    initialState: Option[ClientState],
    handlers: Option[List[Handler[ClientState]]],
    name: Option[String]
  ) {
    import Bot.Config._

    def withToken(token: String): Builder[ClientState, Config with Token] = {
      copy(token = Some(token))
    }

    def withInitialState(state: ClientState): Builder[ClientState, Config with InitialState] = {
      copy(initialState = Some(state))
    }

    def withHandlers(handlers: List[Handler[ClientState]]): Builder[ClientState, Config with Handlers] = {
      copy(handlers = Some(handlers))
    }

    def withName(name: String): Builder[ClientState, Config with Name] = {
      copy(name = Some(name))
    }

    def build(implicit ev: Config =:= All): Bot[ClientState] = {
      Bot(token.get, initialState.get, handlers.get, name.get)
    }
  }
}
