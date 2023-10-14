package xx.xx.xx

object yyyJob {

  private class SparkJob(deltaService: DeltaService)(implicit spark: SparkSession) {

    override def xxx(): DataFrame = {

      val a =
        s"""
           |   aaa
           |   vv
           |""".stripMargin

      val sql =
        s"""with base as (
           |select * from people)
           |select id
           |  FROM base
           | GROUP BY user_id""".stripMargin

      val sql2 =
         """
           | with base2 as (
           |select * from people)
           |select id
           |  FROM base
           | GROUP BY user_id""".stripMargin

      spark.sql(
        """
          | with base2 as (
          |select * from people)
          |select id
          |  FROM base
          | GROUP BY user_id""".stripMargin)

      spark.sql("""
                  | with base2 as (
                  |select * from people)
                  |select id
                  |  FROM base
                  | GROUP BY user_id""".stripMargin
      )

      printAndApplySparkSql(spark, sql)
    }

  }

}