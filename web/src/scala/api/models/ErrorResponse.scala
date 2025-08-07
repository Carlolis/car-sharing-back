package api.models

import zio.json.{JsonDecoder, JsonEncoder}

case class ErrorResponse(message: String) derives JsonEncoder, JsonDecoder
