package domain.models.trip

import sttp.tapir.Codec.PlainCodec
import sttp.tapir.{Schema, ValidationResult, Validator, *}
import zio.json.*
import zio.prelude.Newtype

import java.util.UUID

object TripId extends Newtype[UUID] {
  given (using c: JsonCodec[UUID]): JsonCodec[Type] = derive[JsonCodec]

  given Validator.Primitive[UUID] = Validator.Custom(
    make(_).fold(
      e => ValidationResult.Invalid(e.toList),
      _ => ValidationResult.Valid
    )
  )

  def decode(s: String): DecodeResult[TripId] = DecodeResult.Value(TripId(UUID.fromString(s)))

  def encode(id: TripId): String = id.toString

  given validator(using Validator.Primitive[UUID]): Validator[Type] = derive[Validator]

  given (using s: Schema[UUID]): Schema[Type] = derive[Schema].validate(validator)

  given PlainCodec[TripId] = Codec.string.mapDecode(decode)(encode)
}

type TripId = TripId.Type
