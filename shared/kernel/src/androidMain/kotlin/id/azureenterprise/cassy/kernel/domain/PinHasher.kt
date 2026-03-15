package id.azureenterprise.cassy.kernel.domain

import java.security.MessageDigest

actual class PinHasher {
    actual fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$pin".toByteArray())
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
