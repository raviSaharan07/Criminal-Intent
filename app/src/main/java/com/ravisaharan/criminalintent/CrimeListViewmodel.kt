package com.ravisaharan.criminalintent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

private const val TAG="CrimeListViewModel"

class CrimeListViewmodel : ViewModel() {
    private val crimeRepository=CrimeRepository.get()
    private val _crimes:MutableStateFlow<List<Crime>> = MutableStateFlow(emptyList())
    val crimes: StateFlow<List<Crime>>
        get()=_crimes.asStateFlow()
    init{
        viewModelScope.launch {
          crimeRepository.getCrimes().collect{
              _crimes.value=it

          }
        }
    }
    /*
        suspend fun loadCrimes(): List<Crime>{
            val result= mutableListOf<Crime>()
            delay(5000)

            for(i in 0 until 100){
                val crime=Crime(
                    id= UUID.randomUUID(),
                    title="Crime #$i",
                    date= Date(),
                    isSolved = i % 2 == 0)

                result+=crime
            }
            return result
            return crimeRepository.getCrimes()
            }*/
    }