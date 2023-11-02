package org.coins.sql.format

trait SQLFormatterPluginTrait {
  def formatSQLInString(content: String): String
}
