package org.coins.sql.format

import org.coins.sql.format.regex.RegexHelper.{lastWordToNewLineIfNotOnlyOne, replaceWord, wordToNewLine, wordToUpperCase}

import scala.collection.mutable.Stack

object SQLFormatter {

  private val SQL_KEY_WORDS_LEFT_ALIGNED  = Set("SELECT", "FROM", "WHERE", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "WITH", "GROUP BY", "ORDER BY")
  private val SQL_KEY_WORDS_RIGHT_ALIGNED = Set("AND", "ON")
  private val SQL_KEY_WORDS_NOT_ALIGNED   = Set("ASC", "DESC", "AS")

  private val SQL_KEY_WORDS_STARTED_WITH_NEW_LINE = SQL_KEY_WORDS_LEFT_ALIGNED ++ SQL_KEY_WORDS_RIGHT_ALIGNED
  private val SQL_KEY_WORDS = SQL_KEY_WORDS_STARTED_WITH_NEW_LINE ++ SQL_KEY_WORDS_NOT_ALIGNED
  private val DEFAULT_LEFT_INDENT = " " * 8 + "|"  // 8-space indentation with |

  def findCustomLeftIndent(sql: String): Option[String] = {
    "^(\\s+\\|)".r.findFirstMatchIn(sql).map(m => m.matched.replace("\n", ""))
  }

  def findPrefixForSelect(sql: String): Option[String] = {
    "^(.*)SELECT".r.findFirstMatchIn(sql).map(m => m.group(1))
  }

  def minifySqlString(sql: String): String = {
    sql.stripMargin.split("\n").map(line => line.trim).mkString(" ").trim
  }

  def keyWordsToUpper(sql: String): String = {
    SQL_KEY_WORDS
      .map(_.toLowerCase)
      .foldLeft(sql) { (acc, keyword) => wordToUpperCase(keyword, acc) }
  }

  def keyWordsToNewLine(sql: String): String = {
    SQL_KEY_WORDS_STARTED_WITH_NEW_LINE
      .foldLeft(sql) { (acc, keyword) => wordToNewLine(keyword, acc) }
      .split("\n")
      .map(line => line.trim)
      .mkString("\n")
  }

  def emptyLineBeforeLastSelectIfNotOnlyOne(sql: String): String = {
    lastWordToNewLineIfNotOnlyOne("SELECT", sql)
  }

  def keyWordsAligned(sql: String, leftIndent: Option[String] = None): String = {
    SQL_KEY_WORDS_STARTED_WITH_NEW_LINE
      .foldLeft(sql) { (acc, keyword) =>
        val leftSpacePadding = if (SQL_KEY_WORDS_LEFT_ALIGNED.contains(keyword)) {
          " " * ("SELECT".length - keyword.split(" ").head.length)
        } else if (SQL_KEY_WORDS_RIGHT_ALIGNED.contains(keyword)) {
          " " * "SELECT ".length
        } else {
          throw new RuntimeException(s"Keyword $keyword should be in one of two groups: left aligned or right aligned")
        }
        replaceWord(keyword, s"$leftSpacePadding$keyword", acc)
      }
      .split("\n")
      .mkString(s"\n${leftIndent.getOrElse(DEFAULT_LEFT_INDENT)}")
  }


  def selectFieldsToNewLine(sql: String): String = {
    sql.split("\n")
    .map { line =>
      if (line.stripMargin.trim.startsWith("SELECT")) {
        val selectPrefix = findPrefixForSelect(line)
        formatSelectLine(line, selectPrefix)
      } else {
        line
      }
    }
    .mkString("\n")
  }

  def formatSelectLine(line: String, selectPrefix: Option[String]): String = {
    val parenthesesStack = Stack[Char]() // TODO: check if works with immuatable
    var formattedLine = new StringBuilder

    for (ch <- line) {
      if (ch == '(') parenthesesStack.push(ch)
      if (ch == ')') parenthesesStack.pop
      formattedLine += ch
      if (ch == ',' && parenthesesStack.isEmpty) {
        formattedLine ++= "\n" + selectPrefix.getOrElse("") + " " * ("SELECT ".length - 1)
      }
    }
    formattedLine.toString()
  }
}
