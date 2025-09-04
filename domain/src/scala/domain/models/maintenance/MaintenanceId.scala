package domain.models.maintenance

import sttp.tapir.Codec.PlainCodec
import sttp.tapir.{Schema, ValidationResult, Validator, *}
import zio.json.*
import zio.prelude.Newtype

import java.util.UUID

object MaintenanceId extends Newtype[UUID] {
  given (using c: JsonCodec[UUID]): JsonCodec[Type] = derive[JsonCodec]

  given Validator.Primitive[UUID] = Validator.Custom(
    make(_).fold(
      e => ValidationResult.Invalid(e.toList),
      _ => ValidationResult.Valid
    )
  )

  def decode(s: String): DecodeResult[MaintenanceId] = DecodeResult.Value(MaintenanceId(UUID.fromString(s)))

  def encode(id: MaintenanceId): String = id.toString

  given validator(using Validator.Primitive[UUID]): Validator[Type] = derive[Validator]

  given (using s: Schema[UUID]): Schema[Type] = derive[Schema].validate(validator)

  given PlainCodec[MaintenanceId] = Codec.string.mapDecode(decode)(encode)
}

type MaintenanceId = MaintenanceId.Type
