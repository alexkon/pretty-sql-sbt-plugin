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

  "keyWordsToUpper function" should "convert all keywords to upper string" in {
    val inputSQL =
      """
        |Select *
        |  From selection
        | where id = 1
        | """.stripMargin
    val expectedSQl =
      """
        |SELECT *
        |  FROM selection
        | WHERE id = 1
        | """.stripMargin
    val actualFormattedString = SQLFormatter.keyWordsToUpper(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }
}
