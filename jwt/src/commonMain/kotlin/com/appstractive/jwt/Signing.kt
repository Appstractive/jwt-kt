package com.appstractive.jwt

import com.appstractive.jwt.utils.urlEncoded
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.algorithms.digest.Digest
import dev.whyoleg.cryptography.algorithms.digest.SHA256
import dev.whyoleg.cryptography.algorithms.digest.SHA384
import dev.whyoleg.cryptography.algorithms.digest.SHA512
import dev.whyoleg.cryptography.operations.signature.SignatureGenerator
import kotlinx.serialization.json.JsonObject

enum class Algorithm {
  HS256,
  HS384,
  HS512,
  RS256,
  RS384,
  RS512,
  PS256,
  PS384,
  PS512,
  ES256,
  ES384,
  ES512,
}

val Algorithm.digest: CryptographyAlgorithmId<Digest>
  get() =
      when (this) {
        Algorithm.ES256,
        Algorithm.PS256,
        Algorithm.RS256,
        Algorithm.HS256 -> SHA256

        Algorithm.ES384,
        Algorithm.RS384,
        Algorithm.PS384,
        Algorithm.HS384 -> SHA384

        Algorithm.ES512,
        Algorithm.HS512,
        Algorithm.RS512,
        Algorithm.PS512 -> SHA512
      }

interface SigningAlgorithm {
  suspend fun generator(digest: CryptographyAlgorithmId<Digest>): SignatureGenerator

  suspend fun sign(header: Header, claims: JsonObject): ByteArray {
    val headerEncoded = header.urlEncoded()
    val claimsEncoded = claims.urlEncoded()

    return generator(digest = header.alg.digest)
        .generateSignature("$headerEncoded.$claimsEncoded".encodeToByteArray())
  }
}

@JwtDsl
class Signer {

  private var algorithm: SigningAlgorithm? = null
  internal var type: Algorithm? = null
    private set

  fun algorithm(algorithm: SigningAlgorithm, type: Algorithm) {
    this.algorithm = algorithm
    this.type = type
  }

  internal suspend fun sign(header: Header, claims: JsonObject): ByteArray {
    return algorithm?.sign(
        header = header,
        claims = claims,
    ) ?: throw IllegalStateException("No algorithm configured")
  }
}

fun signer(builder: Signer.() -> Unit): Signer = Signer().apply(builder)

suspend fun UnsignedJWT.sign(kid: String? = null, builder: Signer.() -> Unit): JWT =
    sign(
        kid = kid,
        signer = signer(builder),
    )

suspend fun UnsignedJWT.sign(kid: String? = null, signer: Signer): JWT {
  val finalHeader =
      header.copy(
          alg = checkNotNull(signer.type) { "No algorithm configured" },
          kid = kid,
      )
  return JWT(
      header = finalHeader,
      claims = claims,
      signature =
          signer.sign(
              header = finalHeader,
              claims = claims,
          ),
  )
}
