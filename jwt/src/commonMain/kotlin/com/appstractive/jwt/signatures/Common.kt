package com.appstractive.jwt.signatures

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.algorithms.asymmetric.RSA
import dev.whyoleg.cryptography.algorithms.symmetric.HMAC

internal val provider by lazy { CryptographyProvider.Default }
internal val hmac by lazy { provider.get(HMAC) }
internal val pkcs1: RSA<RSA.PKCS1.PublicKey, RSA.PKCS1.PrivateKey, RSA.PKCS1.KeyPair> by lazy { provider.get(RSA.PKCS1) }
internal val pss: RSA.PSS by lazy { provider.get(RSA.PSS) }
internal val ecdsa: ECDSA by lazy { provider.get(ECDSA) }