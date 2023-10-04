package org.coins.sql.format.regex

object RegexHelper {
  val KEY_INSENSITIVE_KEY = "(?i)"

  def replaceWord(wordOld: String, wordNew: String, target: String, isCaseInsensitive: Boolean = false): String = {
    val pattern = if (isCaseInsensitive) {
      s"$KEY_INSENSITIVE_KEY\\b$wordOld\\b".r
    } else {
      s"\\b$wordOld\\b".r
    }
    pattern.replaceAllIn(target, wordNew)
  }

  def wordToUpperCase(keyword: String, target: String): String = {
    replaceWord(keyword, s"${keyword.toUpperCase}", target, isCaseInsensitive = true)
  }

  def wordToNewLine(keyword: String, target: String): String = {
    replaceWord(keyword, s"\n$keyword", target)
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
