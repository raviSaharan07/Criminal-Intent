package com.ravisaharan.criminalintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.ravisaharan.criminalintent.databinding.FragmentCrimeDetailBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

//private const val TAG="CrimeDetailFragment"

class CrimeDetailFragment : Fragment(){
    private var _binding: FragmentCrimeDetailBinding?=null
    private val binding
        get()= checkNotNull(_binding){"Binding is Null Cannot Access it"}
    //private lateinit var crime:Crime

    private val args:CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewmodel : CrimeDetailViewmodel by viewModels{
        CrimeDetailViewModelFactory(args.crimeId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*crime=Crime(id=UUID.randomUUID(),
            title = "",
            date= Date(),
            isSolved = false)

        Log.d(TAG,"The crime ID is: ${args.crimeId}")*/
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
                //crime=crime.copy(title = text.toString())
            }

            crimeDate.apply {
                //text=crime.date.toString()
                isEnabled=false
            }

            crimeSolved.setOnCheckedChangeListener{_,isChecked ->
                //crime=crime.copy(isSolved = isChecked)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                crimeDetailViewmodel.crime.collect{crime->
                    crime?.let { updateUi(it) }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

    private fun updateUi(crime:Crime){
        binding.apply {
            if(crimeTitle.text.toString()!=crime.title) {
                crimeTitle.setText(crime.title)
            }

            crimeDate.text=crime.date.toString()
            crimeSolved.isChecked=crime.isSolved
            }
        }
}