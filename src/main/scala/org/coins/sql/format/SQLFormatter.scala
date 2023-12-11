package org.coins.sql.format

import org.coins.sql.format.regex.RegexHelper._

import scala.collection.mutable
import java.util.UUID

object SQLFormatter {

  private val SQL_KEY_WORDS_NEW_LINE_LEFT_ALIGNED = Set(
    "SELECT",
    "FROM",
    "WHERE",
    "LEFT JOIN",
    "LEFT OUTER JOIN",
    "RIGHT JOIN",
    "INNER JOIN",
    "GROUP BY",
    "ORDER BY"
  )
  private val SQL_KEY_WORDS_NEW_LINE_RIGHT_ALIGNED = Set("AND", "OR", "ON")
  private val SQL_KEY_WORDS_NEW_LINE = Set("WITH")
  private val SQL_KEY_WORDS_OTHERS = Set("ASC", "DESC", "AS")

  private val SQL_KEY_WORDS_ALIGNED =
    SQL_KEY_WORDS_NEW_LINE_LEFT_ALIGNED ++ SQL_KEY_WORDS_NEW_LINE_RIGHT_ALIGNED
  private val SQL_KEY_WORDS_STARTED_WITH_NEW_LINE = SQL_KEY_WORDS_ALIGNED ++ SQL_KEY_WORDS_NEW_LINE
  private val SQL_KEY_WORDS = SQL_KEY_WORDS_STARTED_WITH_NEW_LINE ++ SQL_KEY_WORDS_OTHERS
  private val DEFAULT_LEFT_INDENT = " " * 8 + "|" // 8-space indentation with |

  private[format] var strLiteralsWithKeyWordsMapping: mutable.Map[String, String] =
    mutable.Map.empty

  def findCustomLeftIndent(sql: String): Option[String] = {
    "^(\\s+\\|)".r.findFirstMatchIn(sql).map(m => m.matched.replace("\n", ""))
  }

  def findPrefixForSelect(sql: String): Option[String] = {
    "^(.*)SELECT".r.findFirstMatchIn(sql).map(m => m.group(1))
  }

  def minifySqlString(sql: String): String = {
    sql.stripMargin.split("\n").map(_.trim).mkString(" ").trim
  }

  def escapeDollarSign(sql: String): String = {
    sql.replace("$", "\\$")
  }

  def replaceStrLiteralsWithKeyWords(sql: String): String = {
    var replacedSql = sql
    val uuid = UUID.randomUUID().toString
    val regex = """'[^']*'""".r
    val stringLiterals = regex.findAllIn(sql).map(_.trim).mkString(",")
    val stringLiteralArr: Set[String] =
      stringLiterals.split(",").toSet
    val sqlKeywords = s"""${SQL_KEY_WORDS.mkString("|")}""".r

    for (stringLiteral <- stringLiteralArr) {
      if (sqlKeywords.findFirstIn(stringLiteral.toUpperCase).isDefined) {
        strLiteralsWithKeyWordsMapping.update(
          stringLiteral,
          stringLiteral.replaceAll("\\s", "").replace("'", "_") + uuid
        )
        replacedSql =
          replacedSql.replaceAll(stringLiteral, strLiteralsWithKeyWordsMapping(stringLiteral))
      }
    }

    replacedSql
  }

  def recoverStrLiteralsWithKeyWords(sql: String): String = {
    var recoveredSql = sql
    if (strLiteralsWithKeyWordsMapping != mutable.Map.empty) {
      for ((k, v) <- strLiteralsWithKeyWordsMapping) {
        recoveredSql = recoveredSql.replaceAll(v, k)
      }
      strLiteralsWithKeyWordsMapping.clear
    }
    recoveredSql
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

  def keyWordsAligned(sql: String): String = {
    SQL_KEY_WORDS_ALIGNED
      .foldLeft(sql) { (acc, keyword) =>
        val leftSpacePadding = if (SQL_KEY_WORDS_NEW_LINE_LEFT_ALIGNED.contains(keyword)) {
          " " * ("SELECT".length - keyword.split(" ").head.length)
        } else if (SQL_KEY_WORDS_NEW_LINE_RIGHT_ALIGNED.contains(keyword)) {
          " " * "SELECT ".length
        } else {
          throw new RuntimeException(
            s"Keyword $keyword should be in one of two groups: left aligned or right aligned"
          )
        }
        replaceWord(keyword, s"$leftSpacePadding$keyword", acc)
      }
  }

  def selectFieldsAlignedToNewLine(sql: String): String = {
    sql
      .split("\n")
      .map { line =>
        if (line.stripMargin.trim.startsWith("SELECT")) {
          val selectPrefix = findPrefixForSelect(line)
          val fieldIndent = selectPrefix.getOrElse("") + (" " * "SELECT ".length)
          replaceHighLevelSymbol(line, ',', s",\n$fieldIndent")
        } else {
          line
        }
      }
      .mkString("\n")
  }

  def replaceHighLevelSymbol(input: String, source: Char, target: String): String = {
    val parenthesesStack = mutable.Stack[Char]()
    val formattedLine = new StringBuilder
    var i = 0

    while (i < input.length) {
      if (input(i) == '(') parenthesesStack.push(input(i))
      if (input(i) == ')') parenthesesStack.pop()
      if (input(i) == source && parenthesesStack.isEmpty) {
        // Move index forward while space is encountered after a comma
        while (i + 1 < input.length && input(i + 1) == ' ') i += 1
        formattedLine ++= target
      } else {
        formattedLine += input(i)
      }
      i += 1 // Move index forward
    }
    formattedLine.toString()
  }

  def applyCustomLeftIndent(sql: String, leftIndent: Option[String] = None): String = {
    sql
      .split("\n")
      .mkString(s"\n${leftIndent.getOrElse(DEFAULT_LEFT_INDENT)}")
  }

  def cteAlignedByWithKeyword(sql: String): String = {
    val isStartsWithKeywordWith = sql.stripMargin.replaceAll("\n", " ").trim.startsWith("WITH")
    if (isStartsWithKeywordWith) {
      val indent = " " * "WITH ".length
      val firstMatchLineNumberWith = lineNumberWithFirstWordMatch("WITH", sql)
      val lastMatchLineNumberSelect = lineNumberWithLastWordMatch("SELECT", sql)
      (firstMatchLineNumberWith, lastMatchLineNumberSelect) match {
        case (Some(start), Some(end)) =>
          sql
            .split("\n")
            .zipWithIndex
            .foldLeft(Seq.empty[String]) { case (res: Seq[String], (line: String, index: Int)) =>
              val newLine =
                if (index > start && index < end && line.nonEmpty) s"$indent$line" else line
              res :+ newLine
            }
            .mkString("\n")
        case _ => sql
      }
    } else {
      sql
    }
  }

  def cteNewLineSeparated(sql: String): String = {
    val (withoutLastSelectSql, lastSelectSql) = lastWordMatchIndexes("SELECT", sql) match {
      case Some((startIndex, _)) => sql.splitAt(startIndex)
      case None                  => (sql, "")
    }
    replaceHighLevelSymbol(withoutLastSelectSql, ',', ",\n\n") + lastSelectSql
  }
}
