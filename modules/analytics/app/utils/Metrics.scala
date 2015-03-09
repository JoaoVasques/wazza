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

  def arpuCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_Arpu_${applicationName}"

  def avgRevenueSessionCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_avgRevenueSession_${applicationName}"

  def avgPurchasesUserCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_avgPurchasesUser_${applicationName}"

  def avgSessionsPerUserCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_numberSessionsPerUser_${applicationName}"

  def lifeTimeValueCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_LifeTimeValue_${applicationName}"

  def sessionsBetweenPurchasesCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_NumberSessionsBetweenPurchases_${applicationName}"

  def averagePurchasePerSessionCollection(
    companyName: String,
    applicationName: String
  ) = s"${companyName}_PurchasesPerSession_${applicationName}"

  def sessionsFirstPurchase(
    companyName: String, 
    applicationName: String
  ) = s"${companyName}_NumberSessionsFirstPurchase_${applicationName}"
}

