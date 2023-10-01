package org.coins.sql.format

import org.coins.sql.format.regex.RegexHelper.{wordToUpperCase, wordToNewLine}

object SQLFormatter {

  private val SQL_KEY_WORDS = Set("SELECT", "FROM", "WHERE", "LEFT", "RIGHT", "INNER", "JOIN", "WITH", "GROUP BY", "ORDER BY", "AND")

  def minifySqlString(sql: String): String = {
    sql.stripMargin.split("\n").map(line => line.trim).mkString(" ").trim
  }

  def keyWordsToUpper(sql: String): String = {
    SQL_KEY_WORDS
      .map(_.toLowerCase)
      .foldLeft(sql) { (acc, keyword) => wordToUpperCase(keyword, acc) }
  }

  def keyWordsToNewLine(sql: String): String = {
    SQL_KEY_WORDS
      .foldLeft(sql) { (acc, keyword) => wordToNewLine(keyword, acc) }
      .split("\n")
      .map(line => line.trim)
      .mkString("\n")
  }

  def keyWordsAligned(sql: String): String = {
    SQL_KEY_WORDS
      .foldLeft(sql) { (acc, keyword) =>
        val leftSpacePadding = " " * ("SELECT".length - keyword.split(" ").head.length)
        acc.replace(keyword, s"$leftSpacePadding$keyword")
      }
      .split("\n")
      .mkString("\n        |") // 8-space indentation with |
  }
}
