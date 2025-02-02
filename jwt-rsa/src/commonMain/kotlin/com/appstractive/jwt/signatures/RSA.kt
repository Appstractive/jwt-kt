package com.appstractive.jwt.signatures

import dev.whyoleg.cryptography.algorithms.RSA

class RSASigningConfig {
  internal var privateKey: ByteArray? = null
  internal var format: RSA.PrivateKey.Format? = null

  fun pem(key: ByteArray) {
    privateKey = key
    format = RSA.PrivateKey.Format.PEM.Generic
  }

  fun pem(key: String) {
    privateKey = key.encodeToByteArray()
    format = RSA.PrivateKey.Format.PEM.Generic
  }

  fun der(key: ByteArray) {
    privateKey = key
    format = RSA.PrivateKey.Format.DER.Generic
  }

  fun der(key: String) {
    privateKey = key.encodeToByteArray()
    format = RSA.PrivateKey.Format.DER.Generic
  }
}

class RSAVerifierConfig {
  internal var publicKey: ByteArray? = null
  internal var format: RSA.PublicKey.Format? = null

  fun pem(key: ByteArray) {
    publicKey = key
    format = RSA.PublicKey.Format.PEM
  }

  fun pem(key: String) {
    publicKey = key.encodeToByteArray()
    format = RSA.PublicKey.Format.PEM
  }

  fun der(key: ByteArray) {
    publicKey = key
    format = RSA.PublicKey.Format.DER
  }

  fun der(key: String) {
    publicKey = key.encodeToByteArray()
    format = RSA.PublicKey.Format.DER
  }
}
