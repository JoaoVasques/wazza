package models.application

trait ApplicationErrors {
  def AlreadyExistsError = "Already exists"
  def DoesNotExistError = "Application does not exist"
  def ApplicationWithNameExistsError(name: String) = s"Application with $name already exists"
}
