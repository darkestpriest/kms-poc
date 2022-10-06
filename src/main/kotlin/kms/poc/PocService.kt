package kms.poc

import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec

class PocService(
    private val kms: KmsClient,
    private val keyId: String
) {

    fun decrypt(encryptedData: ByteArray): String {
        return kms.decrypt { it.keyId(keyId)
            .encryptionAlgorithm(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_256)
            .ciphertextBlob(SdkBytes.fromByteArray(encryptedData))
        }.plaintext().asUtf8String()
    }

    fun encrypt(unsecure: String): ByteArray {
        return kms.encrypt { it.keyId(keyId)
            .encryptionAlgorithm(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_256)
            .plaintext(SdkBytes.fromUtf8String(unsecure))
        }.ciphertextBlob().asByteArray()
    }
}