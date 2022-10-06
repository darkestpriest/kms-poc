package kms.poc

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.kms.KmsClient

@Suppress("unused")
class Handler(
    private val service: PocService
): RequestHandler<Any, Unit> {

    companion object {
        private val log = LoggerFactory.getLogger(Handler::class.java)
        private const val toEncrypt = "any-text"
        private const val KEY_ID = "KEY_ID"
    }

    constructor(): this(
        PocService(KmsClient.create(), System.getProperty(KEY_ID, System.getenv(KEY_ID)).apply {
            if (this.isNullOrBlank()) throw IllegalArgumentException("Missing environment variable: $KEY_ID")
        })
    )

    override fun handleRequest(input: Any?, context: Context?) {
        log.info("about to encrypt $toEncrypt")
        val secured = service.encrypt(toEncrypt)
        log.info("encrypt result ${secured.decode()}")
        val decrypted = service.decrypt(secured)
        log.info("decrypt result $decrypted")
    }

    private fun ByteArray.decode() = kotlin.runCatching {
        this.decodeToString()
    }.getOrElse { "cannot show" }
}