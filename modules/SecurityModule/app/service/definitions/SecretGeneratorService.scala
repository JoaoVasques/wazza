package service.security.definitions

trait SecretGeneratorService {

  def generateSecret(secretType: Int): String
}

package object SecretGeneratorServiceContext {
  final val Id = 0
  final val ApiKey = 1
}
