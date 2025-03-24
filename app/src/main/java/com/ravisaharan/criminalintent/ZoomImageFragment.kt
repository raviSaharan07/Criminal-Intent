package com.ravisaharan.criminalintent

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import java.io.File

class ZoomImageFragment : DialogFragment() {

    private val args: ZoomImageFragmentArgs by navArgs()
    private lateinit var imageView: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val dialogView = inflater.inflate(R.layout.dialog_zoom_image,null)

            imageView = dialogView.findViewById(R.id.crime_photo)

            val photoFile = File(requireContext().applicationContext.filesDir,args.photoFileName)
            val bitmap = BitmapFactory.decodeFile(photoFile.path)
            imageView.setImageBitmap(bitmap)
            /*if(photoFile.exists()){
                imageView.doOnLayout { measureView ->
                    //val scaledBitmap = getScaledBitmap(photoFile.path,measureView.width,measureView.height)

                }
            }else{
                imageView.setImageBitmap(null)
            }*/

            builder.setView(dialogView)
                .setPositiveButton(getString(R.string.dismiss), DialogInterface.OnClickListener{ dialog, id ->
                    dismiss()
                })

            builder.create()


        }?:throw IllegalStateException("Activity cannot be null")
    }


}