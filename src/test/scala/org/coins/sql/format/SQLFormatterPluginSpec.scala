package org.coins.sql.format

import org.scalatest.flatspec.{AnyFlatSpec => FlatSpec}
import org.scalatest.matchers.should.Matchers

class SQLFormatterPluginSpec extends FlatSpec with Matchers {

  "formatSQLString function" should "return well formatted SQL String for trivial select" in {
    val inputSQL = """select * from people"""
    val expectedSQl =
      """
        |SELECT *
        |FROM people"""
    SQLFormatterPlugin.formatSQLString(inputSQL) shouldBe expectedSQl
  }
}
