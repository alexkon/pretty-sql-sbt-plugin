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

  "keyWordsToNewLine function" should "return multiline SQL string where each keyword started with new line" in {
    val inputSQL = "SELECT * FROM SELECTION WHERE id = 1"
    val expectedSQl =
      """
        |SELECT *
        |FROM SELECTION
        |WHERE id = 1""".stripMargin
    val actualFormattedString = SQLFormatter.keyWordsToNewLine(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "keyWordsAligned function" should "return multiline SQL string with default aligned indent" in {
    val inputSQL =
      """
        |SELECT *
        |FROM SELECTION
        |WHERE id = 1""".stripMargin
    val expectedSQl =
      """
        |SELECT *
        |  FROM SELECTION
        | WHERE id = 1"""
    val actualFormattedString = SQLFormatter.keyWordsAligned(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "keyWordsAligned function" should "return multiline SQL string with custom aligned indent" in {
    val inputSQL =
      """
        |SELECT *
        |FROM SELECTION
        |WHERE id = 1""".stripMargin
    val expectedSQl ="""
   |SELECT *
   |  FROM SELECTION
   | WHERE id = 1"""
    val actualFormattedString = SQLFormatter.keyWordsAligned(inputSQL, leftIndent = Some("   |"))

    actualFormattedString shouldBe expectedSQl
  }

  "keyWordsAligned function" should "return well aligned SQL string with 'AND' and 'ON' keywords" in {
    val inputSQL =
      """
        |SELECT *
        |FROM USERS
        |LEFT JOIN SYSTEM_USERS
        |ON USERS.id = SYSTEM_USERS.id
        |WHERE 1=1
        |AND id = 1
        |AND age > 0""".stripMargin
    val expectedSQl =
      """
        |SELECT *
        |  FROM USERS
        |  LEFT JOIN SYSTEM_USERS
        |       ON USERS.id = SYSTEM_USERS.id
        | WHERE 1=1
        |       AND id = 1
        |       AND age > 0"""
    val actualFormattedString = SQLFormatter.keyWordsAligned(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "selectFieldsToNewLine function" should "put new line after each field with appropriate indent" in {
    val inputSQL =
      """
        |SELECT id, cast(coalesce(age, 0) as int) as age, name
        |  FROM USERS""".stripMargin
    val expectedSQl =
      """
        |SELECT id,
        |       cast(coalesce(age, 0) as int) as age,
        |       name
        |  FROM USERS""".stripMargin
    val actualFormattedString = SQLFormatter.selectFieldsToNewLine(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }
}
