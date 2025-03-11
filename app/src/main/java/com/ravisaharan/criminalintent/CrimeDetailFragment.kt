package com.ravisaharan.criminalintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.ravisaharan.criminalintent.databinding.FragmentCrimeDetailBinding
import java.util.Date
import java.util.UUID

private const val TAG="CrimeDetailFragment"

class CrimeDetailFragment : Fragment(){
    private var _binding: FragmentCrimeDetailBinding?=null
    private val binding
        get()= checkNotNull(_binding){"Binding is Null Cannot Access it"}
    private lateinit var crime:Crime

    private val args:CrimeDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        crime=Crime(id=UUID.randomUUID(),
            title = "",
            date= Date(),
            isSolved = false)

        Log.d(TAG,"The crime ID is: ${args.crimeId}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ):View?{

        _binding=FragmentCrimeDetailBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            crimeTitle.doOnTextChanged{text,_,_,_ ->
                crime=crime.copy(title = text.toString())
            }

            crimeDate.apply {
                text=crime.date.toString()
                isEnabled=false
            }

            crimeSolved.setOnCheckedChangeListener{_,isChecked ->
                crime=crime.copy(isSolved = isChecked)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}