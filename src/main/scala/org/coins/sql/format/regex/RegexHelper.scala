package org.coins.sql.format.regex

object RegexHelper {

  def replaceWord(wordOld: String, wordNew: String, target: String): String = {
    s"\\b$wordOld\\b".r.replaceAllIn(target, wordNew)
  }

  def wordToUpperCase(keyword: String, target: String): String = {
    s"(?i)\\b$keyword\\b".r.replaceAllIn(target, s"${keyword.toUpperCase}")
  }

  def wordToNewLine(keyword: String, target: String): String = {
    s"\\b$keyword\\b".r.replaceAllIn(target, s"\n$keyword")
  }

  def lastWordToNewLineIfNotOnlyOne(word: String, sql: String): String = {
    val pattern = s"\\b$word\\b".r
    val matches = pattern.findAllMatchIn(sql).toList

    if (matches.nonEmpty && matches.length > 1) {   // if match found and it's not the only one
      val lastMatch = matches.last
      val (start, end) = (lastMatch.start, lastMatch.end)
      val (before, after) = (sql.substring(0, start), sql.substring(end))
      s"$before\n$word$after"
    } else sql
  }
}
