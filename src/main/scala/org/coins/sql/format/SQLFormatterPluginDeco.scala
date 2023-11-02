package org.coins.sql.format

import org.coins.sql.format.SQLFormatterPlugin.{formatSQLInString => fmtSQLString}

import java.time.Instant
import java.util.UUID

object SQLFormatterPluginDeco extends SQLFormatterPluginTrait {
  private def dollarSignReplacement(): String = {
    val uuid = UUID.randomUUID
    val timestamp = Instant.now().toEpochMilli
    "#" + timestamp.toString + uuid + "#"
  }
  override def formatSQLInString(content: String): String = {
    val replacement = dollarSignReplacement()
    val tmpContent: String = content.replace("$", replacement)
    fmtSQLString(tmpContent).replace(replacement, "$")
  }
}
