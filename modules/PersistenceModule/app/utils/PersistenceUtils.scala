package utils.persistence

import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.SecureRandom
import org.bson.types.ObjectId

object PersistenceUtils {

  private lazy val MongoObjectIdBytes = 12

  def idToLong(_id: ObjectId): Long = {
    ByteBuffer.wrap(_id.toByteArray()).getLong()
  }

  def longToId(idLong: Long): ObjectId = {
    val byteArray = ByteBuffer.allocate(MongoObjectIdBytes).putLong(idLong).array()
    new ObjectId(byteArray)
  }
}

