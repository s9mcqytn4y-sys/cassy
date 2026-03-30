package id.azureenterprise.cassy.kernel.domain

expect open class PinHasher() {
    open fun hash(pin: String, salt: String): String
}
