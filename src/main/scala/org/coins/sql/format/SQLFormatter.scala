package org.coins.sql.format

import java.util.regex.Pattern

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
      .foldLeft(sql) { (acc, keyword) => keywordToNewLine(keyword, acc) }
//      .foldLeft(sql) { (acc, keyword) => acc.replace(keyword, s"\n$keyword") }
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

  private def wordToUpperCase(keyword: String, target: String): String = {
    s"(?i)\\b$keyword\\b".r.replaceAllIn(target, _ => s"${keyword.toUpperCase}")
  }

  private def keywordToNewLine(keyword: String, target: String): String = {
    s"\\b$keyword\\b".r.replaceAllIn(target, _ => s"\n$keyword")
  }
}
