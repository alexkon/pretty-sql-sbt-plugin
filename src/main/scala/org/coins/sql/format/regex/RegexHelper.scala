package org.coins.sql.format.regex

import scala.annotation.tailrec
import scala.util.matching.Regex

object RegexHelper {
  val KEY_INSENSITIVE_KEY = "(?i)"
  val STRING_LITERAL_REGEXP: Regex = """'[^']*'""".r

  def replaceWord(
      wordOld: String,
      wordNew: String,
      target: String,
      isCaseInsensitive: Boolean = false
  ): String = {
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

    if (matches.nonEmpty && matches.length > 1) { // if match found and it's not the only one
      val lastMatch = matches.last
      val (start, end) = (lastMatch.start, lastMatch.end)
      val (before, after) = (sql.substring(0, start), sql.substring(end))
      s"$before\n$word$after"
    } else sql
  }

  def lastWordMatchIndexes(word: String, sql: String): Option[(Int, Int)] = {
    val pattern = s"\\b$word\\b".r
    val matches = pattern.findAllMatchIn(sql).toList

    if (matches.nonEmpty) {
      val lastMatch = matches.last
      Some((lastMatch.start, lastMatch.end))
    } else None
  }

  def lineNumberWithFirstWordMatch(word: String, sql: String): Option[Int] = {
    @tailrec
    def loop(lines: Seq[String], lineIndex: Int, pattern: Regex): Option[Int] = {
      if (lineIndex >= lines.size) {
        None
      } else {
        val currentLine = lines(lineIndex)
        pattern.findFirstMatchIn(currentLine) match {
          case Some(_) => Some(lineIndex)
          case None    => loop(lines, lineIndex + 1, pattern)
        }
      }
    }

    val pattern: Regex = s"\\b$word\\b".r
    val lines = sql.split("\n")
    loop(lines, 0, pattern)
  }

  def lineNumberWithLastWordMatch(word: String, sql: String): Option[Int] = {
    val lines = sql.split("\n")
    val sqlReversedLines = lines.reverse.mkString("\n")
    val firstMatch = lineNumberWithFirstWordMatch(word, sqlReversedLines)
    firstMatch match {
      case Some(index) => Some(lines.size - 1 - index)
      case None        => firstMatch
    }
  }

  def getStringLiteralMap(sql: String): Map[String, String] = {
    STRING_LITERAL_REGEXP.findAllIn(sql).zipWithIndex.map{ case (str, index) => (s"'s$index'", str)}.toMap
  }
}
