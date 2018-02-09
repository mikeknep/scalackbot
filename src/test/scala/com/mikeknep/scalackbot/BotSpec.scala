package com.mikeknep.scalackbot

import org.scalatest.FunSpec
import org.scalatest.Matchers._

class BotSpec extends FunSpec {
  describe("Building a bot") {
    it("is built using a Builder providing an API token, name, list of handlers, and an initial state") {
      """val bot: Bot[Int] = Bot.builder()
        |.withToken("apiToken")
        |.withName("myBot")
        |.withHandlers(List.empty)
        |.withInitialState(0)
        |.build""".stripMargin should compile
    }

    describe("without required fields") {
      it("will not compile without an API token") {
        """val bot: Bot[Int] = Bot.builder()
          |.withName("myBot")
          |.withHandlers(List.empty)
          |.withInitialState(0)
          |.build""".stripMargin shouldNot typeCheck
      }

      it("with not compile without a name") {
        """val bot: Bot[Int] = Bot.builder()
          |.withToken("apiToken")
          |.withHandlers(List.empty)
          |.withInitialState(0)
          |.build""".stripMargin shouldNot typeCheck
      }

      it("will not compile without handlers") {
        """val bot: Bot[Int] = Bot.builder()
          |.withToken("apiToken")
          |.withName("myBot")
          |.withInitialState(0)
          |.build""".stripMargin shouldNot typeCheck
      }

      it("will not compile without an initial state") {
        """val bot: Bot[Int] = Bot.builder()
          |.withToken("apiToken")
          |.withName("myBot")
          |.withHandlers(List.empty)
          |.build""".stripMargin shouldNot typeCheck
      }
    }
  }
}
