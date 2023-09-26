package org.coins.sql.format

import org.scalatest.flatspec.{AnyFlatSpec => FlatSpec}
import org.scalatest.matchers.should.Matchers

class SQLFormatterPluginSpec extends FlatSpec with Matchers {

  "formatSQLString function" should "return well formatted SQL String for trivial select" in {
    val inputSQL = """select * from people"""
    val expectedSQl =
      """
        |SELECT *
        |  FROM people"""
    val actualFormattedString = SQLFormatterPlugin.formatSQLString(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "formatSQLString function" should "return well formatted SQL String for filter statement" in {
    val inputSQL = """select * from people where id = 1"""
    val expectedSQl =
      """
        |SELECT *
        |  FROM people
        | WHERE id = 1"""
    val actualFormattedString = SQLFormatterPlugin.formatSQLString(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "formatSQLString function" should "return well formatted SQL String for GROUP BY statement" in {
    val inputSQL = """select age, count(1) as total from people group by 1 order by 2 desc"""
    val expectedSQl =
      """
        |SELECT age, count(1) as total
        |  FROM people
        | GROUP BY 1
        | ORDER BY 2 desc"""
    val actualFormattedString = SQLFormatterPlugin.formatSQLString(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }
}
