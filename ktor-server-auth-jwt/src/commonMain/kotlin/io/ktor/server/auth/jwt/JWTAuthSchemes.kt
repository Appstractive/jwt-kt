﻿/*
 * Copyright 2014-2021 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.auth.jwt

internal class JWTAuthSchemes(val defaultScheme: String, vararg additionalSchemes: String) {
  val schemes = (arrayOf(defaultScheme) + additionalSchemes).toSet()
  val schemesLowerCase = schemes.map { it.lowercase() }.toSet()

  operator fun contains(scheme: String): Boolean = scheme.lowercase() in schemesLowerCase
}
