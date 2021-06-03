package com.example.gestionnairedechantiers.materiel

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.gestionnairedechantiers.MainActivity
import com.example.gestionnairedechantiers.R
import com.example.gestionnairedechantiers.databinding.GestionMaterielFragmentBinding
import com.example.gestionnairedechantiers.utils.hideKeyboard
import com.google.android.material.datepicker.MaterialDatePicker
import com.theartofdev.edmodo.cropper.CropImage
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

class GestionMaterielFragment : Fragment() {

    private val PERMISSION_CODE = 1000

    private val viewModel: GestionMaterielViewModel by navGraphViewModels(R.id.gestionMaterielNavGraph)


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = GestionMaterielFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        // Create the date picker builder and set the title
        val builder = MaterialDatePicker.Builder.datePicker()
        // create the date picker
        val datePicker = builder.build()
        // set listener when date is selected
        datePicker.addOnPositiveButtonClickListener {


            viewModel.onDateSelected(Date(it))
        }

        viewModel.navigation.observe(viewLifecycleOwner, { navigation ->
            hideKeyboard(activity as MainActivity)
            when (navigation) {
                GestionMaterielViewModel.NavigationMenu.ANNULATION -> {
                    val action =
                        GestionMaterielFragmentDirections.actionGestionMaterielFragmentPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionMaterielViewModel.NavigationMenu.ENREGISTREMENT_MATERIEL -> {
                    val action =
                        GestionMaterielFragmentDirections.actionGestionMaterielFragmentPop()
                    findNavController().navigate(action)
                    viewModel.onBoutonClicked()
                }
                GestionMaterielViewModel.NavigationMenu.AJOUT_PHOTO -> {
                    selectImage()
                    viewModel.onBoutonClicked()
                }
                GestionMaterielViewModel.NavigationMenu.CHOIX_DATE_IMMAT -> {
                    datePicker.show(activity?.supportFragmentManager!!, "MyTAG")
                    viewModel.onBoutonClicked()
                }
                GestionMaterielViewModel.NavigationMenu.EN_ATTENTE -> {}
                else -> Timber.e("ERROR NAVIGATION GESTIONMATERIELFRAGMENT $navigation")

            }

        })

        return binding.root
    }

    private fun selectImage() {

        //if system os is Marshmallow or Above, we need to request runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_DENIED
            ) {
                //permission was not enabled
                val permission = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                //show popup to request permission
                requestPermissions(permission, PERMISSION_CODE)
            } else {
                //permission already granted
                CropImage.activity()
                    .setAspectRatio(1, 1)
                    .start(requireContext(), this);
            }
        } else {
            //system os is < marshmallow
            CropImage.activity()
                .start(requireContext(), this);
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //called when image was captured from camera intent
        when (requestCode) {

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    result.uri?.let { uri ->
                        val cachePhotoFile = File(uri.path!!)
                        saveCroppedImage(cachePhotoFile)
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Timber.e("Crop error: ${result.error}")
                }
            }
        }
    }


    private fun saveCroppedImage(file: File) {
        Timber.i("path: $file")
        // val cachePhotoFile = File(uri.toString())
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }

        file.copyTo(photoFile!!, true)// This method will be executed once the timer is over
        viewModel.ajoutPathImage(photoFile.absolutePath.toString())
    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            Timber.i("path: $currentPhotoPath")
        }
    }

}
