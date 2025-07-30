package models

import zio.json.*
import zio.prelude.Newtype

import java.util.UUID

object TripId extends Newtype[UUID] {
  given (using c: JsonCodec[UUID]): JsonCodec[Type] = derive[JsonCodec]
}

type TripId = TripId.Type
