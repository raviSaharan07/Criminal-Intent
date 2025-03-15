package com.ravisaharan.criminalintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ravisaharan.criminalintent.databinding.FragmentCrimeDetailBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID

//private const val TAG="CrimeDetailFragment"

class CrimeDetailFragment : Fragment(){
    private var _binding: FragmentCrimeDetailBinding?=null

    private val binding
        get()= checkNotNull(_binding){"Binding is Null Cannot Access it"}

    private val args:CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewmodel : CrimeDetailViewmodel by viewModels{
        CrimeDetailViewModelFactory(args.crimeId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            if(binding.crimeTitle.text.isBlank()){
                binding.crimeTitle.error="Title is Required"
            }else{
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        callback.isEnabled = true
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
                crimeDetailViewmodel.updateCrime {oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            crimeSolved.setOnCheckedChangeListener{_,isChecked ->
                crimeDetailViewmodel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                crimeDetailViewmodel.crime.collect{crime->
                    crime?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(DatePickerFragment.REQUEST_KEY_DATE){_,bundle ->
                val newDate = Date(bundle.getLong(DatePickerFragment.BUNDLE_KEY_DATE))
                crimeDetailViewmodel.updateCrime { it.copy(date = newDate) }
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
            crimeDate.setOnClickListener{
                findNavController().navigate(CrimeDetailFragmentDirections.selectDate(crime.date))
            }
            crimeSolved.isChecked=crime.isSolved
            }
        }
}