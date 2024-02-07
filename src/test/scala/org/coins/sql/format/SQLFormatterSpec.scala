package org.coins.sql.format

import org.coins.sql.format.regex.RegexHelper
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
        | WHERE id = 1""".stripMargin
    val actualFormattedString = SQLFormatter.keyWordsAligned(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "keyWordsAligned function" should "return multiline SQL string with custom aligned indent" in {
    val inputSQL =
      """
    |SELECT *
    |FROM SELECTION
    |WHERE id = 1""".stripMargin
    val expectedSQl =
      """
    |SELECT *
    |  FROM SELECTION
    | WHERE id = 1""".stripMargin
    val actualFormattedString = SQLFormatter.keyWordsAligned(inputSQL)

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
        |       AND age > 0""".stripMargin
    val actualFormattedString = SQLFormatter.keyWordsAligned(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "selectFieldsToNewLine function" should "put new line after each field with appropriate indent" in {
    val inputSQL =
      """
        |SELECT id,cast(coalesce(age, 0) as int) as age, first_name,  last_name
        |  FROM USERS""".stripMargin
    val expectedSQl =
      """
        |SELECT id,
        |       cast(coalesce(age, 0) as int) as age,
        |       first_name,
        |       last_name
        |  FROM USERS""".stripMargin
    val actualFormattedString = SQLFormatter.selectFieldsAlignedToNewLine(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "applyCustomLeftIndent function" should "return multiline SQL string with custom aligned indent" in {
    val inputSQL =
      """
        |SELECT *
        |  FROM SELECTION
        | WHERE id = 1""".stripMargin
    val expectedSQl = """
   |SELECT *
   |  FROM SELECTION
   | WHERE id = 1"""
    val actualFormattedString =
      SQLFormatter.applyCustomLeftIndent(inputSQL, leftIndent = Some("   |"))

    actualFormattedString shouldBe expectedSQl
  }

  "cteAlignedByWithKeyword function" should "return well formatted SQL with right indent for CTE expressions" in {
    val inputSQL =
      """
    |WITH base as (
    |SELECT *
    |  FROM people)
    |
    |SELECT id
    |  FROM base""".stripMargin

    val expectedSQl =
      """
        |WITH base as (
        |     SELECT *
        |       FROM people)
        |
        |SELECT id
        |  FROM base""".stripMargin
    val actualFormattedString = SQLFormatter.cteAlignedByWithKeyword(inputSQL)

    actualFormattedString shouldBe expectedSQl
  }

  "literalReplacementMap function" should "return key-value Map with key: ('s<index>') and value: ('<literal>')" in {
    val inputSQL = """select 'select a from b' as sql1, 'select c from d' as sql2 from selection"""
    val expectedMap = Map("'s0'" -> "'select a from b'", "'s1'" -> "'select c from d'")

    val actualMap = RegexHelper.literalReplacementMap(inputSQL)
    actualMap shouldBe expectedMap
  }

  "replaceLiterals function" should "return SQL all string literal replaced by keys ('s<index>')'" in {
    val inputSQL = """select 'select a from b' as sql1, 'select c from d' as sql2 from selection"""
    val expectedSQl = """select 's0' as sql1, 's1' as sql2 from selection"""

    val actualFormattedString = SQLFormatter.replaceLiterals(inputSQL)
    actualFormattedString shouldBe expectedSQl
  }

  "recoverLiterals function" should "return SQL string recoverd by stringLiteralTempMap " in {
    val inputSQL = """select 's0' as sql1, 's1' as sql2 from selection"""
    val expectedSQl = """select 'select a from b' as sql1, 'select c from d' as sql2 from selection"""
    val replacementMap = Map("'s0'" -> "'select a from b'", "'s1'" -> "'select c from d'")

    val actualFormattedString = SQLFormatter.recoverLiterals(inputSQL, replacementMap)
    actualFormattedString shouldBe expectedSQl
  }

  "functions recoverLiterals and recoverLiterals" should "return original string after sequential apply" in {
    val inputSQL = """select 'select a from b' as sql1, 'select c from d' as sql2 from selection"""

    val replacedStr = SQLFormatter.replaceLiterals(inputSQL)
    val recoveredStr = SQLFormatter.recoverLiterals(replacedStr, RegexHelper.literalReplacementMap(inputSQL))

    inputSQL shouldBe recoveredStr
  }
}
