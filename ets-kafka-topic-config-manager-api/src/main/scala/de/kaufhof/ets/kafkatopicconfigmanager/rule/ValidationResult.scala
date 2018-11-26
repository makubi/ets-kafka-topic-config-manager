package de.kaufhof.ets.kafkatopicconfigmanager.rule

sealed trait ValidationResult {
  def +(other: ValidationResult): ValidationResult = {
    (this, other) match {
      case (ValidationSuccess, ValidationSuccess) => ValidationSuccess
      case (error, ValidationSuccess) => error
      case (ValidationSuccess, error) => error
      case (errors1: ValidationErrors, errors2: ValidationErrors) => ValidationErrors(errors1.errors ++ errors2.errors)
    }
  }

  def ++(others: ValidationResult*): ValidationResult = {
    others.foldLeft[ValidationResult](this)( (prev, cur) =>
      prev + cur
    )
  }

  def ++(others: Array[ValidationResult]): ValidationResult = {
    ++(others:_*)
  }

  def ++(others: Iterable[ValidationResult]): ValidationResult = {
    ++(others.toArray)
  }

  def flatten(xs: List[Any]): List[Any] = xs match {
    case Nil => Nil
    case (head: List[_]) :: tail => flatten(head) ++ flatten(tail)
    case head :: tail => head :: flatten(tail)
  }

}
case object ValidationSuccess extends ValidationResult
case class ValidationErrors(errors: Iterable[ValidationError]) extends ValidationResult

trait ValidationError {
  val shortDescription: String = getClass.getSimpleName
  val longDescription: String = toString
}
