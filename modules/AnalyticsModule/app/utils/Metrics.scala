package utils.analytics

object Metrics {

  def totalRevenueCollection(
    applicationName: String,
    companyName: String
  ) = s"${applicationName}_TotalRevenue_${companyName}"

  def avgSessionLengthCollection(
    applicationName: String,
    companyName: String
  ) = s"${applicationName}_SessionLength_${companyName}"

  def numberPayingUsers (
    applicationName: String,
    companyName: String
  ) = s"${applicationName}_nrPayingUsers_${companyName}"
}

