package org.coins.sql.format

import org.scalatest.flatspec.{AnyFlatSpec => FlatSpec}
import org.coins.sql.format.SQLFormatterPlugin.formatSQLInString
import org.scalatest.matchers.should.Matchers
import scala.util.Random

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
        |SELECT age,
        |       count(1) AS total
        |  FROM people
        | GROUP BY 1
        | ORDER BY 2 DESC"""
    val actualFormattedString = SQLFormatterPlugin.formatSQLString(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "formatSQLString function" should "return well formatted SQL String with custom left indent" in {
    val randomNumberFrom1to10 = Random.nextInt(10) + 1
    val customLeftIndent = " " * randomNumberFrom1to10 + "|"

    val inputSQL = s"""
${customLeftIndent}select * from people
${customLeftIndent}"""

    val expectedSQl = s"""
${customLeftIndent}SELECT *
${customLeftIndent}  FROM people"""

    val actualFormattedString = SQLFormatterPlugin.formatSQLString(inputSQL)
    actualFormattedString shouldBe expectedSQl
  }

  "formatSQLString function" should "return well formatted SQL String with empty line before last SELECT" in {
    val inputSQL =
      """
        |WITH base as (
        |     SELECT *
        |       FROM people)
        |SELECT id
        |  FROM base
        |""".stripMargin

    val expectedSQl =
      """
        |WITH base AS (
        |     SELECT *
        |       FROM people)
        |
        |SELECT id
        |  FROM base"""
    val actualFormattedString = SQLFormatterPlugin.formatSQLString(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "formatSQLString function" should "return well formatted SQL with empty line separated cte" in {
    val inputSQL =
      """
        |WITH base1 as (SELECT *FROM people),
        |     base2 as (SELECT *FROM people)
        |SELECT * FROM base1
        |INNER JOIN base2 using(id)""".stripMargin

    val expectedSQl =
      """
        |WITH base1 AS (
        |     SELECT *
        |       FROM people),
        |
        |     base2 AS (
        |     SELECT *
        |       FROM people)
        |
        |SELECT *
        |  FROM base1
        | INNER JOIN base2 using(id)"""
    val actualFormattedString = SQLFormatterPlugin.formatSQLString(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  /** `formatSQLInString` needs to implement the following features at the same time：
    *   1. none sql string should not be affected. 2. Support both three double quotes or a single
    *      double quote wrapped sql statement.
    */
  "formatSQLInString" should "support situations as comments mentioned" in {
    val threeDoubleQuotes = "\"\"\""
    val content =
      s"""package xx.xx.xx
         |object xxxJob {
         |  private class SparkJob(deltaService: DeltaService)(implicit spark: SparkSession) {
         |    val str = "string should not be impacted"
         |    val sql1 = "  select * from user"
         |    spark.sql("select * from user")
         |    spark.sql($threeDoubleQuotes select * from user$threeDoubleQuotes)
         |    val sql2 = $threeDoubleQuotes
         |                 |-- this is a comment
         |                 |select * from people$threeDoubleQuotes
         |  }
         |}""".stripMargin
    val actualFormattedContent: String = formatSQLInString(content)

    val expectedString = s"""package xx.xx.xx
                            |object xxxJob {
                            |  private class SparkJob(deltaService: DeltaService)(implicit spark: SparkSession) {
                            |    val str = "string should not be impacted"
                            |    val sql1 = "  select * from user"
                            |    spark.sql("select * from user")
                            |    spark.sql($threeDoubleQuotes
                            |        |SELECT *
                            |        |  FROM user$threeDoubleQuotes.stripMargin)
                            |    val sql2 = $threeDoubleQuotes-- this is a comment
                            |                 |SELECT *
                            |                 |  FROM people$threeDoubleQuotes.stripMargin
                            |  }
                            |}""".stripMargin

    actualFormattedContent shouldBe expectedString
  }
}
