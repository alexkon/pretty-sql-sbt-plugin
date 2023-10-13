package xx.xx.xx

object xxxJob {

  private class SparkJob(deltaService: DeltaService)(implicit spark: SparkSession) {

    override def xxx(): DataFrame = {

      val spark.sql(s"""
           |with base as (
           |  select * from people)
           |  select id
           |  FROM base
           | GROUP BY user_id""".stripMargin
      )

      printAndApplySparkSql(spark, sql)
    }

  }

}
