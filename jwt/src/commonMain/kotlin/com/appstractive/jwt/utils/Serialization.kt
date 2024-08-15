package com.appstractive.jwt.utils

import kotlinx.serialization.json.Json

internal val json = Json {
  isLenient = true
  ignoreUnknownKeys = true
  explicitNulls = false
  encodeDefaults = true
}
