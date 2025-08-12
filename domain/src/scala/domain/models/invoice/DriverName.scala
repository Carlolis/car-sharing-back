package domain.models.invoice


import sttp.tapir.Codec.PlainCodec
import sttp.tapir.{Schema, ValidationResult, Validator, *}
import zio.json.*
import zio.prelude.Newtype

import java.util.UUID

object DriverName extends Newtype[String] {
  given (using c: JsonCodec[String]): JsonCodec[Type] = derive[JsonCodec]

  given Validator.Primitive[String] = Validator.Custom(
    make(_).fold(
      e => ValidationResult.Invalid(e.toList),
      _ => ValidationResult.Valid
    )
  )
  def decode(s: String): DecodeResult[DriverName] = DecodeResult.Value(DriverName(s))

  def encode(driverName: DriverName): String = driverName.toString

  given validator(using Validator.Primitive[String]): Validator[Type] = derive[Validator]

  given (using s: Schema[String]): Schema[Type] = derive[Schema].validate(validator)

  given PlainCodec[DriverName] = Codec.string.mapDecode(decode)(encode)


  given JsonFieldEncoder[DriverName] = new JsonFieldEncoder[DriverName] {
    def unsafeEncodeField(in: DriverName): String = in.toString
  }

  given JsonFieldDecoder[DriverName]= new JsonFieldDecoder[DriverName] {
    override def unsafeDecodeField(trace: List[JsonError],in: String): DriverName = DriverName(in)
}



}

type DriverName = DriverName.Type
