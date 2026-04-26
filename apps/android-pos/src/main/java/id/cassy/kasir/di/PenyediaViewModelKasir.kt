package id.cassy.kasir.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PenyediaViewModelKasir : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        throw IllegalArgumentException("ViewModel belum tersedia untuk ${modelClass.name}")
    }
}
