package org.coins.sql.format

import org.coins.sql.format.SQLFormatterPlugin.formatSQLInString
import org.scalatest.flatspec.{AnyFlatSpec => FlatSpec}
import org.scalatest.matchers.should.Matchers
import sbt.File
import sbt.io.IO

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

  "formatSQLInString function" should "return well formatted SQL with irregular sql string fragment" in {
    val rawExpectedString =
      """package xx.xx.xx
        |
        |object xxxJob {
        |
        |  private class SparkJob(deltaService: DeltaService)(implicit spark: SparkSession) {
        |
        |    override def xxx(): DataFrame = {
        |
        |      val spark.sql(s\"\"\"
        |        |WITH base AS (
        |        |     SELECT *
        |        |       FROM people)
        |        |
        |        |SELECT id
        |        |  FROM base
        |        | GROUP BY user_id\"\"\".stripMargin
        |      )
        |
        |      printAndApplySparkSql(spark, sql)
        |    }
        |
        |  }
        |
        |}
        |""".stripMargin
    val expectedString = rawExpectedString.replace("\\", "")
    val content = IO.read(new File("output/single_sql_statement_contained_demo2.scala"))
    val actualFormattedContent: String = formatSQLInString(content)

    actualFormattedContent shouldBe expectedString
  }

  "formatSQLInString function" should "return well formatted SQL which parsed from sql stated like sql = " in {
    val rawExpectedString =
      """package xx.xx.xx
        |
        |object xxxJob {
        |
        |  private class SparkJob(deltaService: DeltaService)(implicit spark: SparkSession) {
        |
        |    override def xxx(): DataFrame = {
        |
        |      val sql =
        |        s\"\"\"
        |        |WITH usd_php_rate_with_rn AS (
        |        |     SELECT *,
        |        |            row_number() OVER (PARTITION BY base, quote
        |        |      ORDER BY update_at DESC) AS rn
        |        |       FROM #ratesTable
        |        |      WHERE `date` = '#{config.date}' )
        |        |
        |        |SELECT base,
        |        |       quote,
        |        |       rate,
        |        |       update_at
        |        |  FROM usd_php_rate_with_rn
        |        | WHERE rn = 1\"\"\".stripMargin
        |
        |      printAndApplySparkSql(spark, sql)
        |    }
        |
        |  }
        |
        |}
        |""".stripMargin
    val expectedString = rawExpectedString.replace("\\", "")
    val content = IO.read(new File("output/single_sql_statement_contained_demo1.scala"))
    val actualFormattedContent: String = formatSQLInString(content)

    actualFormattedContent shouldBe expectedString
  }

  "formatSQLInString function" should "return well and pretty formatted SQL which parsed from multiple sqls stated by different ways" in {
    val rawExpectedString =
      """package xx.xx.xx
        |
        |object yyyJob {
        |
        |  private class SparkJob(deltaService: DeltaService)(implicit spark: SparkSession) {
        |
        |    override def xxx(): DataFrame = {
        |
        |      val a =
        |        s\"\"\"
        |           |   aaa
        |           |   vv
        |           |\"\"\"
        |
        |      val sql =
        |        s\"\"\"
        |           |WITH base AS (
        |           |     SELECT *
        |           |       FROM people)
        |           |
        |           |SELECT id
        |           |  FROM base
        |           | GROUP BY user_id\"\"\".stripMargin
        |
        |      val sql2 =
        |         \"\"\"
        |           |WITH base2 AS (
        |           |     SELECT *
        |           |       FROM people)
        |           |
        |           |SELECT id
        |           |  FROM base
        |           | GROUP BY user_id\"\"\".stripMargin
        |
        |      spark.sql(
        |        \"\"\"
        |          |WITH base2 AS (
        |          |     SELECT *
        |          |       FROM people)
        |          |
        |          |SELECT id
        |          |  FROM base
        |          | GROUP BY user_id\"\"\".stripMargin)
        |
        |      spark.sql(\"\"\"
        |                  |WITH base2 AS (
        |                  |     SELECT *
        |                  |       FROM people)
        |                  |
        |                  |SELECT id
        |                  |  FROM base
        |                  | GROUP BY user_id\"\"\".stripMargin
        |      )
        |
        |      printAndApplySparkSql(spark, sql)
        |    }
        |
        |  }
        |
        |}""".stripMargin
    val expectedString = rawExpectedString.replace("\\", "")
    val content = IO.read(new File("output/multi_sql_statement_contained_demo.scala"))
    val actualFormattedContent: String = formatSQLInString(content)

    actualFormattedContent shouldBe expectedString
  }

  "formatSQLInString debug" should "print in console" in {
    val content = IO.read(new File("output/multi_sql_statement_contained_demo.scala"))
    val actualFormattedContent: String = formatSQLInString(content)
    println(actualFormattedContent)
  }
}
