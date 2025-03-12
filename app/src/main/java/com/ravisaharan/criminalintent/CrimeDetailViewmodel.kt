package com.ravisaharan.criminalintent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CrimeDetailViewmodel(crimeId:UUID) : ViewModel() {

    private val crimeRepository=CrimeRepository.get()

    private val _crime:MutableStateFlow<Crime?> = MutableStateFlow(null)

    val crime: StateFlow<Crime?>
        get()=_crime.asStateFlow()

    init{
        viewModelScope.launch{
            _crime.value=crimeRepository.getCrime(crimeId)
        }
    }

}

@Suppress("UNCHECKED_CAST")
class CrimeDetailViewModelFactory(private val crimeId:UUID): ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CrimeDetailViewmodel(crimeId) as T
    }
}