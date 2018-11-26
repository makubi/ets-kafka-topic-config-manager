package de.kaufhof.ets.kafkatopicconfigmanager.rule

import de.kaufhof.ets.kafkatopicconfigmanager.rule.topic.{Environment, Topic}

import scala.language.implicitConversions

sealed trait Rule {

  def validate(): ValidationResult

  type ValuesDifferFunction[T] = (T, T, Environment, Topic) => ValidationError
  type ValueMissingFunction[T] = (T, Environment, Topic) => ValidationError

  protected def compareOptionalValues[T <: AnyVal](optionalXmlValue: Option[T], optionalKafkaValue: Option[T])
                                        (valuesDiffer: ValuesDifferFunction[T], kafkaValueMissing: ValueMissingFunction[T], xmlValueMissing: ValueMissingFunction[T])
                                        (implicit environment: Environment, topic: Topic): ValidationResult = {

    (optionalXmlValue, optionalKafkaValue) match {

      case (Some(xmlValue), Some(kafkaValue)) =>
        compareWithErrorResult(xmlValue, kafkaValue, valuesDiffer)

      case (Some(xmlValue), None) =>
        kafkaValueMissing(xmlValue, environment, topic)

      case (None, Some(kafkaValue)) =>
        xmlValueMissing(kafkaValue, environment, topic)

      case (None, None) =>
        ValidationSuccess

    }
  }

  protected implicit def validationErrorToValidationErrors(validationError: ValidationError): ValidationErrors =
    ValidationErrors(Iterable(validationError))

  protected def compareWithErrorResult[T <: AnyVal](value1: T, value2: T, error: (T, T, Environment, Topic) => ValidationError)(implicit environment: Environment, topic: Topic): ValidationResult = {
    compareAnyWithErrorResult(value1, value2, error)
  }

  protected def compareComparableWithErrorResult[T <: Comparable[T]](value1: T, value2: T, error: (T, T, Environment, Topic) => ValidationError)(implicit environment: Environment, topic: Topic): ValidationResult = {
    compareAnyWithErrorResult(value1, value2, error)
  }

  protected implicit def validationResultIterableToValidationResult(validationResultIterable: Iterable[ValidationResult]): ValidationResult = {
    validationResultIterable.foldLeft[ValidationResult](ValidationSuccess)( (prev, cur) =>
      prev + cur
    )
  }

  private def compareAnyWithErrorResult[T](value1: T, value2: T, error: (T, T, Environment, Topic) => ValidationError)(implicit environment: Environment, topic: Topic): ValidationResult = {
    if (value1 == value2) ValidationSuccess
    else error(value1, value2, environment, topic)
  }
}

trait ConfigurationComparison extends Rule
trait XmlConfigurationValidation extends Rule