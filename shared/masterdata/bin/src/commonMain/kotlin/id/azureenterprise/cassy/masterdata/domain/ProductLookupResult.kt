package id.azureenterprise.cassy.masterdata.domain

sealed class ProductLookupResult {
    data class FoundSingle(val product: Product) : ProductLookupResult()
    data object NotFound : ProductLookupResult()
    data object Collision : ProductLookupResult()
    data object Unavailable : ProductLookupResult()
    data class InvalidInput(val message: String) : ProductLookupResult()
}
