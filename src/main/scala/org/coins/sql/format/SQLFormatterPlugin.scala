package org.coins.sql.format

import sbt.Keys._
import sbt._

object SQLFormatterPlugin extends AutoPlugin {

  object autoImport {
    val formatSQL = taskKey[Unit]("Format SQL embedded in Scala strings")
  }

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[_]] = Seq(
    formatSQL := {
      val log = streams.value.log
      val baseDir = baseDirectory.value
      formatScalaFiles(baseDir, log)
    }
  )

  def formatScalaFiles(baseDir: File, log: Logger): Unit = {
    val scalaFiles = baseDir ** "*.scala"

    scalaFiles.get.foreach { file =>
      val content = IO.read(file)
      val formattedContent = formatSQLInString(content)
      IO.write(file, formattedContent)
      log.info(s"Formatted SQL in $file")
    }
  }

  def formatSQLInString(content: String): String = {
    val sqlPattern = """spark\.sql\(\n?\s*s?\"\"\"(?s)(.*?)\"\"\"\.stripMargin\)""".r
    sqlPattern.replaceAllIn(content, m => {
      val sql = m.group(1)
      val formattedSQL = formatSQLString(sql)
      s"""spark.sql(s\"\"\"$formattedSQL\"\"\".stripMargin)"""
    })
  }

  def formatSQLString(sql: String): String = {
    val keywords = Set("SELECT", "FROM", "WHERE", "LEFT JOIN", "INNER JOIN", "WITH", "GROUP BY", "ORDER BY", "AND", ",")
    keywords.foldLeft(sql) { (acc, keyword) =>
      acc.replace(keyword, s"\n$keyword")
    }.split("\n").map(line => line.trim).mkString("\n    ") // 4-space indentation
  }
}