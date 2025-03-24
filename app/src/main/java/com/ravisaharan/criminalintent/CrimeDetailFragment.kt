package com.ravisaharan.criminalintent

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
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
import java.io.File
import java.io.FilePermission
import java.util.Calendar
import java.util.Date
import java.util.UUID

//private const val TAG="CrimeDetailFragment"
private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeDetailFragment : Fragment(){
    private var _binding: FragmentCrimeDetailBinding?=null

    private val binding
        get()= checkNotNull(_binding){"Binding is Null Cannot Access it"}

    private val args:CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewmodel : CrimeDetailViewmodel by viewModels{
        CrimeDetailViewModelFactory(args.crimeId)
    }

    private val  selectSuspect = registerForActivityResult(ActivityResultContracts.PickContact()){uri ->
        uri?.let{parseContactSelection(it)}
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()){
        didTakePhoto : Boolean ->
            if(didTakePhoto && photoName!=null){
                crimeDetailViewmodel.updateCrime { oldCrime ->
                    oldCrime.copy(photoFileName = photoName)
                }
            }
    }

    private var photoName :String? =  null

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

            crimeSuspect.setOnClickListener{
                selectSuspect.launch(null)
            }

            val suspectIntent = selectSuspect.contract.createIntent(requireContext(),null)
            crimeSuspect.isEnabled = canResolveIntent(suspectIntent)

            crimeCamera.setOnClickListener{
                photoName="IMG_${Date()}.JPG"
                val photoFile= File(requireContext().applicationContext.filesDir,photoName)
                val photoUri= FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    photoFile
                )

                takePhoto.launch(photoUri)
            }

            val cameraCaptureIntent=takePhoto.contract.createIntent(requireContext(), Uri.EMPTY)
            crimeCamera.isEnabled=canResolveIntent(cameraCaptureIntent)
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

            crimeReport.setOnClickListener{
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type="text/plain"
                    putExtra(Intent.EXTRA_SUBJECT,getString(R.string.crime_report_subject))
                    putExtra(Intent.EXTRA_TEXT,getCrimeReport(crime))

                }

                val chooserIntent=Intent.createChooser(reportIntent,getString(R.string.send_report))

                startActivity(chooserIntent)
            }

            crimeSuspect.text=crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }

            updatePhoto(crime.photoFileName)


        }


    }

    private fun getCrimeReport(crime:Crime) : String {
        val solvedString = if(crime.isSolved){
            getString(R.string.crime_report_solved)
        }else{
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT,crime.date).toString()

        val suspectString=if(crime.suspect.isBlank()){
            getString(R.string.crime_report_no_suspect)
        }else{
            getString(R.string.crime_report_suspect,crime.suspect)
        }

        return getString(R.string.crime_report,crime.title,dateString,solvedString,suspectString)
    }

    private fun parseContactSelection(contactUri : Uri){
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        val queryCursor=requireActivity().contentResolver.query(contactUri,queryFields,null,null,null)

        queryCursor?.use {cursor ->
            if(cursor.moveToFirst()){
                val suspect = cursor.getString(0)
                crimeDetailViewmodel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect)
                }
            }
        }
    }

    private fun canResolveIntent(intent : Intent) : Boolean{
        val packageManager : PackageManager=requireActivity().packageManager
        val resolveInfo : ResolveInfo?=packageManager.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo!=null
    }

    private fun updatePhoto(photoFileName : String?){
        if(binding.crimePhoto.tag!=photoFileName){
            val photoFile = photoFileName?.let{
                File(requireContext().applicationContext.filesDir,it)
            }

            if(photoFile?.exists()==true){
                binding.crimePhoto.doOnLayout { measureView ->
                    val scaledBitmap = getScaledBitmap(photoFile.path,
                        measureView.width,
                        measureView.height)

                    binding.crimePhoto.setImageBitmap(scaledBitmap)
                    binding.crimePhoto.tag=photoFileName
                }
            }else{
                binding.crimePhoto.setImageBitmap(null)
                binding.crimePhoto.tag=null
            }
        }
    }
}