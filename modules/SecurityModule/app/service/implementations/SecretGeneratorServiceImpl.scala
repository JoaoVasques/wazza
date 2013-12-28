package service.security.implementations

import service.security.definitions._
import java.security.SecureRandom
import java.math.BigInteger
import SecretGeneratorServiceContext._
import java.util.UUID

class SecretGeneratorServiceImpl extends SecretGeneratorService {

  private final val NumberBits = 130
  private final val Radix = 32

  def generateSecret(secretType: Int): String = {
    secretType match {
      case Id => {
        val random = new SecureRandom()
        new BigInteger(NumberBits, random).toString(Radix)
      }
      case ApiKey => {
        UUID.randomUUID().toString().filterNot("-".toSet)
      }
      case _ => null
    }
  }
}
