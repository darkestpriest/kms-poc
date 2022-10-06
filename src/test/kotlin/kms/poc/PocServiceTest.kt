package kms.poc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.EncryptionAlgorithmSpec

@Disabled
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PocServiceTest {
    companion object {
        @Container
        @JvmStatic
        private val container = DockerImageName.parse("localstack/localstack:0.11.3").let {
            LocalStackContainer(it).withServices(LocalStackContainer.Service.KMS)
        }

        private const val fixed = "fixed text"

    }

    private lateinit var sut: PocService
    private lateinit var kmsClient: KmsClient

    @BeforeAll
    fun setup() {
        kmsClient = KmsClient.builder().endpointOverride(container.getEndpointOverride(LocalStackContainer.Service.KMS))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("dummy", "dummy"))).build()
    }

    @Test
    fun `can decrypt a cypher text`() {
        val (key, cipher) = prepareTestCase()
        sut = PocService(
            kmsClient,
            key
        )

        Assertions.assertEquals(fixed, sut.decrypt(cipher))
    }

    private fun prepareTestCase(): Pair<String, ByteArray> {
        val keyId = kmsClient.createKey { it
                .description("testing key")
                .customKeyStoreId("RSA_2048")
                .keyUsage("ENCRYPT_DECRYPT")
        }.keyMetadata().keyId()
        val secured = kmsClient.encrypt { it
            .keyId(keyId)
            .encryptionAlgorithm(EncryptionAlgorithmSpec.RSAES_OAEP_SHA_256)
            .plaintext(SdkBytes.fromUtf8String(fixed))
        }
        return keyId to secured.ciphertextBlob().asByteArrayUnsafe()
    }
}