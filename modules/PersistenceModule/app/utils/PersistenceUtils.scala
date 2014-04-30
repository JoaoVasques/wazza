package utils.persistence

import com.fasterxml.uuid.Generators
import java.util.UUID;

object PersistenceUtils {

  private lazy val MongoObjectIdBytes = 12

  /**
  def idToLong(_id: ObjectId): Long = {
    ByteBuffer.wrap(_id.toByteArray()).getLong()
  }

  def longToId(idLong: Long): ObjectId = {
    val byteArray = ByteBuffer.allocate(MongoObjectIdBytes).putLong(idLong).array()
    new ObjectId(byteArray)
  }
  **/

  def generateId() = {
    val uuid_gen = Generators.timeBasedGenerator()
    uuid_gen.generate.timestamp
  }
}

