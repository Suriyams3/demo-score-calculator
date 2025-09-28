// src/main/scala/com/myplatform/Job1_ScoreCalculator.scala

package com.myplatform

import org.apache.spark.sql.functions._
import org.apache.spark.sql.{SparkSession, SaveMode}

object Job1_ScoreCalculator {

  def main(args: Array[String]): Unit = {
    // 1. Initialize Spark Session (Spark Context is implicitly available in Glue)
    val spark = SparkSession.builder
      .appName("Job1_ScoreCalculator")
      .getOrCreate()

    // 2. Configuration Parameters (Passed via Glue Job Arguments)
    // Example: s3://my-platform-input-bucket/input/
    val inputPath = args(0)
    // Example: s3://my-platform-intermediate-bucket/csv_output/
    val outputPath = args(1)

    // Define the weighted metrics for the engagement score
    val WEIGHT_LOGIN = 0.4
    val WEIGHT_VIEWS = 0.3
    val WEIGHT_CARTS = 0.3

    // 3. Read the Input Parquet Data
    val activityDF = spark.read
      .parquet(inputPath) // Assuming inputPath points to the daily Parquet file or folder
      .withColumnRenamed("user_id_col", "UserID") // Align column name for output

    // 4. Transformation Logic: Calculate the Engagement Score
    val scoredDF = activityDF
      .withColumn(
        "Calculated_Score",
        // Normalize and weight the raw activity metrics (e.g., scale them down/up)
        col("login_count") * WEIGHT_LOGIN +
        col("items_viewed") * WEIGHT_VIEWS * 0.1 + // items_viewed might be higher, so we scale it down
        col("cart_additions") * WEIGHT_CARTS * 1.5 // cart_additions is a strong signal, so we scale it up
      )
      // Select the required columns and add a Timestamp
      .select(
        col("UserID"),
        col("Calculated_Score").cast("decimal(10, 4)"),
        current_timestamp().as("Timestamp")
      )
      // Ensure the score is not negative (though logic prevents it) and cap it at a reasonable max
      .withColumn("Calculated_Score", when(col("Calculated_Score") > 10.0, 10.0).otherwise(col("Calculated_Score")))


    // 5. Write the Output CSV File to S3
    scoredDF.write
      .mode(SaveMode.Overwrite)
      .option("header", "true")
      .csv(outputPath)

    spark.stop()
  }
}