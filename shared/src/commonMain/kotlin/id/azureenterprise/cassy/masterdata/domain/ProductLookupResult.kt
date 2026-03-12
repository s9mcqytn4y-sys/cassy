package id.azureenterprise.cassy.masterdata.domain

sealed class ProductLookupResult {
    data class FoundSingle(val product: Product) : ProductLookupResult()
    object NotFound : ProductLookupResult()
    object Collision : ProductLookupResult()
    object Unavailable : ProductLookupResult()
    data class InvalidInput(val message: String) : ProductLookupResult()
}
