package org.coins.sql.format.regex

object RegexHelper {

  def wordToUpperCase(keyword: String, target: String): String = {
    s"(?i)\\b$keyword\\b".r.replaceAllIn(target, _ => s"${keyword.toUpperCase}")
  }

  def wordToNewLine(keyword: String, target: String): String = {
    s"\\b$keyword\\b".r.replaceAllIn(target, _ => s"\n$keyword")
  }
}
