package notifications.plugin.actors

 trait MailActor {

   def sendEmail(subject: String, to: List[String], message: String): Unit
}

