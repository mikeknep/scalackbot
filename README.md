# Scalackbot
[![Build Status](https://travis-ci.org/mikeknep/scalackbot.svg?branch=master)](https://travis-ci.org/mikeknep/scalackbot)

Scalackbot is a framework for building Slack bots in Scala.

## Usage

1. Define the state you care about
2. Define handlers that operate on that state in response to Slack messages
3. Build your bot with an initial state and a list of handlers (and some other details)
4. Run it!

To ground these steps in reality, each will be explained in the context of a hypothetical bot for managing code reviews.
People can be added to a list of code reviewers that the bot manages, and the bot can assign a pull request to the next reviewer in line.


#### State

Your bot can keep track of any kind of state you like.
This can be a `class` or a `case class`, depending on how much you like (im)mutability.
Our code review bot simply needs to keep track of a list of reviewers:

```scala
case class ReviewerBotState(reviewers: List[String])
```


#### Handlers

Each handler is responsible for a specific action the bot can perform.
Let's dive into each part of the [Handler trait][handler-trait].

First, `isRelevantMessage(data: SlackMessage): Boolean` determines whether the incoming message from a Slack user should trigger this action.
Typically this just involves checking for a keyword or phrase in the message body text.

If the above check returns `true`, then `perform(state: ClientState, data: SlackMessage): Handler.DefiniteOutcome[ClientState]` is called.
This function provides all the in-the-moment data you need: the current state, and the current "heard" Slack message.
The return value, `Handler.DefiniteOutcome[ClientState]`, holds a new/updated state and a response that the bot will send back to Slack.

Lastly, the trait requires you define `val helpText: String`, which is simply a static description of what the handler does and how to use it.
If no handler is called, the bot will [respond with a help message][help-message-test] that aggregates each handler's `helpText` description.

Putting it all together, our code review bot needs two handlers—one to add new reviewers to the group, and another to assign pull requests to the next reviewer in line:

```scala
object AddReviewerHandler extends Handler[ReviewerBotState] {
  // I recommend highlighting the keyword or phrase required to trigger the handler
  val helpText: String = "*add* someone to the list of available code reviewers"

  def isRelevantMessage(data: SlackMessage): Boolean => {
    data.text contains "add"
  }

  def perform(state: ReviewerBotState, data: SlackMessage): Handler.DefiniteOutcome[ReviewerBotState] = {
    val newReviewer = parseReviewerName(data.text)
    val response = SlackResponse(s"$newReviewer has been added!")
    val newState = state.copy(reviewers = state.reviewers :+ newReviewer)

    new Handler.DefiniteOutcome(newState, response)
  }

  private def parseReviewerName(messageBody: String): String = /* details elided */ "John"
}


object AssignReviewerHandler extends Handler[ReviewerBotState] {
  val helpText: String = "*assign* a PR to the next available code reviewer"

  def isRelevantMessage(data: SlackMessage): Boolean => {
    data.text contains "assign"
  }

  def perform(state: ReviewerBotState, data: SlackMessage): Handler.DefiniteOutcome[ReviewerBotState] = {
    val nextReviewer = state.reviewers.head
    val response = SlackResponse(s"Hey $nextReviewer, you are up next for this PR!")
    val newState = state.copy(reviewers = state.reviewers.tail :+ nextReviewer)

    new Handler.DefiniteOutcome(newState, response)
  }
}
```


#### Building the bot

The bot requires four details:

1. An initial state.
2. A list of all the handlers. **Note**: at most [only one handler will be called in response to a message][only-one-handler-test], so the order of the handlers in this list matters. If a heard message would return `true` for two handlers' `isRelevantMessage` implementations, only the first handler is called.
3. An API token (see Slack's [official bot documentation][slack-bot-docs]).
4. A name. Scalackbots will [only react to messages that address/reference them by name][must-address-bot-test].

All four must be present—your project [will not compile][bot-compile-test] if any are missing.

```scala
val handlers = List(AddReviewerHandler, AssignReviewerHandler)
val emptyState = ReviewerBotState(List.empty)
val bot = Bot.builder()
  .withName("reviewerbot")
  .withToken(System.getenv("BOT_API_TOKEN"))
  .withHandlers(handlers)
  .withInitialState(emptyState)
  .build
```


#### Running the bot

```scala
Production.run(bot)
```


### Testing

To run the bot, we pass it to a runner function, `Production#run`.
We can also pass a constructed bot to a different kind of runner: `IntegrationTesting#run`.
The testing runner also takes a list of messages representative of a conversation going on in the channel.
After running through all the messages, `IntegrationTesting#run` returns an `IntegrationTestResult` struct exposing the final resulting state and a log of replies from the bot.

Note that if you build your bot outside of your `main` method, you can pass _the very same bot_ you'll run in production to the integration test runner, preventing production configuration from potentially straying from test configuration without breaking stale tests.



[bot-compile-test]: https://github.com/mikeknep/scalackbot/blob/ea53dc077227e2a369ec98175935416d8574844d/src/test/scala/com/mikeknep/scalackbot/BotSpec.scala#L17-L49
[handler-trait]: src/main/scala/com/mikeknep/scalackbot/Handler.scala
[help-message-test]: https://github.com/mikeknep/scalackbot/blob/ea53dc077227e2a369ec98175935416d8574844d/src/test/scala/com/mikeknep/scalackbot/CoreSpec.scala#L101-L108
[must-address-bot-test]: https://github.com/mikeknep/scalackbot/blob/7825386f8c1f4572e28f95e929e6fa085199de9a/src/test/scala/com/mikeknep/scalackbot/CoreSpec.scala#L42-L56
[only-one-handler-test]: https://github.com/mikeknep/scalackbot/blob/7825386f8c1f4572e28f95e929e6fa085199de9a/src/test/scala/com/mikeknep/scalackbot/CoreSpec.scala#L73-L80
[slack-bot-docs]: https://api.slack.com/bot-users
