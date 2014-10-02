package utils.analytics

object Metrics {

  def totalRevenueCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_TotalRevenue_${applicationName}"

  def avgSessionLengthCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_SessionLength_${applicationName}"

  def payingUsersCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_payingUsers_${applicationName}"

  def activeUsersCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_activeUsers_${applicationName}"

  def numberSessionsCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_numberSessions_${applicationName}"

  def mobileSessionsCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_mobileSessions_${applicationName}"

  def numberSessionsPerUserCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_numberSessionsPerUser_${applicationName}"
}

