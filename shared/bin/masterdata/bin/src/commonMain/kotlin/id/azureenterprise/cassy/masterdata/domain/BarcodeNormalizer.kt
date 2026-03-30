package id.azureenterprise.cassy.masterdata.domain

class BarcodeNormalizer {
    fun normalize(barcode: String): String {
        return barcode.trim().lowercase()
    }
}
