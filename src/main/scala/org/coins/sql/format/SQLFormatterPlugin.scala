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

      // Your SQL formatting logic goes here
      // For now, we'll just log a message
      log.info("Formatting SQL embedded in Scala strings...")
      // You can use libraries like scalafmt to help with the formatting
    }
  )
}