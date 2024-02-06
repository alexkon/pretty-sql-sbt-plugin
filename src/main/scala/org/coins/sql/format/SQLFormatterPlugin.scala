package org.coins.sql.format

import org.coins.sql.format.regex.RegexHelper.literalReplacementMap
import sbt.Keys._
import sbt._

object SQLFormatterPlugin extends AutoPlugin {

  object autoImport {
    val formatSQL =
      inputKey[Unit]("Format SQL embedded in Scala strings, optionally in a specified file.")
  }

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    formatSQL := {
      val log = streams.value.log
      val baseDir = baseDirectory.value
      val inputFileName = Def.spaceDelimited("<arg>").parsed.headOption

      inputFileName match {
        case Some(fileName) =>
          val file = baseDir / fileName
          if (file.exists() && fileName.endsWith(".scala")) {
            formatScalaFile(file, log)
          } else log.error(s"File $fileName does not exist or is not a Scala file")

        case None => formatScalaFiles(baseDir, log)
      }
    }
  )

  def formatScalaFiles(baseDir: File, log: Logger): Unit = {
    val scalaFiles = (baseDir ** "*.scala").get
    scalaFiles.foreach(file => formatScalaFile(file, log))
  }

  def formatScalaFile(file: File, log: Logger): Unit = {
    val content = IO.read(file)
    val formattedContent = formatSQLInString(content)
    IO.write(file, formattedContent)
    log.info(s"Formatted SQL in $file")
  }

  def formatSQLInString(content: String): String = {
    val sqlPattern = """"{3}(?si)([^"]*?select[^"]*?)"{3}""".r
    sqlPattern
      .replaceAllIn(
        content,
        m => {
          val sql = m.group(1)
          val formattedSQL = formatSQLString(sql)
          s"""\"\"\"$formattedSQL\"\"\".stripMargin"""
        }
      )
  }

  def formatSQLString(sql: String): String = {
    import SQLFormatter._

    val customLeftIndent: Option[String] = findCustomLeftIndent(sql)
    val literalMap: Map[String, String] =  literalReplacementMap(sql)

    Some(sql)
      .map(replaceLiterals)
      .map(minifySqlString)
      .map(keyWordsToUpper)
      .map(cteNewLineSeparated)
      .map(keyWordsToNewLine)
      .map(emptyLineBeforeLastSelectIfNotOnlyOne)
      .map(keyWordsAligned)
      .map(selectFieldsAlignedToNewLine)
      .map(cteAlignedByWithKeyword)
      .map(applyCustomLeftIndent(_, customLeftIndent))
      .map(recoverLiterals(_, literalMap))
      .map(escapeDollarSign)
      .getOrElse(
        throw new RuntimeException(
          "Unexpected behaviour: function `formatSQLString` should return String!"
        )
      )
  }
}
