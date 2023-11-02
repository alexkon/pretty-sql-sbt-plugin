package org.coins.sql.format

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
    val sqlPattern = """\"{1,3}(?si)(.*?select.*?)\"{1,3}""".r
    val sections: Array[String] = content.split("=")
    println(sections.toList)
    val strBuilder: StringBuilder = new StringBuilder
    for (section <- sections) {
      val newSection: String = sqlPattern.replaceAllIn(
        section,
        m => {
          val sql = m.group(1)
          val formattedSQL = formatSQLString(sql)
          s"""\"\"\"$formattedSQL\"\"\".stripMargin"""
        }
      )
      strBuilder.append(newSection + "=")
    }
    strBuilder.toString().dropRight(1)
  }

  def formatSQLString(sql: String): String = {
    import SQLFormatter._

    val customLeftIndent: Option[String] = findCustomLeftIndent(sql)

    Some(sql)
      .map(minifySqlString)
      .map(keyWordsToUpper)
      .map(cteNewLineSeparated)
      .map(keyWordsToNewLine)
      .map(emptyLineBeforeLastSelectIfNotOnlyOne)
      .map(keyWordsAligned)
      .map(selectFieldsAlignedToNewLine)
      .map(cteAlignedByWithKeyword)
      .map(applyCustomLeftIndent(_, customLeftIndent))
      .getOrElse(
        throw new RuntimeException(
          "Unexpected behaviour: function `formatSQLString` should return String!"
        )
      )
  }
}
