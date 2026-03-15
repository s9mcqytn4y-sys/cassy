package id.azureenterprise.cassy.kernel.domain

expect class PinHasher() {
    fun hash(pin: String, salt: String): String
}
