package xx.xx.xx

object xxxJob {

  private class SparkJob(deltaService: DeltaService)(implicit spark: SparkSession) {

    override def xxx(): DataFrame = {

      val sql =
        s"""
         |WITH usd_php_rate_with_rn AS (
         |      SELECT *,
         |             row_number() OVER (PARTITION BY base, quote ORDER BY update_at DESC) AS rn
         |        FROM #ratesTable
         |       WHERE `date` = '#{config.date}'
         | )
         |
         | SELECT base,
         |        quote,
         |        rate,
         |        update_at
         |             FROM usd_php_rate_with_rn
         |  WHERE rn = 1
          """.stripMargin

      printAndApplySparkSql(spark, sql)
    }

  }

}
