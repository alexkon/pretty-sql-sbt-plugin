package org.coins.sql.format

import org.scalatest.flatspec.{AnyFlatSpec => FlatSpec}
import org.scalatest.matchers.should.Matchers

class SQLFormatterSpec extends FlatSpec with Matchers {

  "minifySqlString function" should "return one line SQL string" in {
    val inputSQL =
      """
        |select *
        |  from people
        | where id = 1
        | """.stripMargin
    val expectedSQl = "select * from people where id = 1"
    val actualFormattedString = SQLFormatter.minifySqlString(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }
}
