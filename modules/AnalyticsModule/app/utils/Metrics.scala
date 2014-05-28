package utils.analytics

object Metrics {

  def totalRevenueCollection(
    applicationName: String,
    companyName: String
  ) = s"${applicationName}_TotalRevenue_${companyName}"

}

