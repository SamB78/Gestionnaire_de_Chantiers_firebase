package com.techphone78.gestionnairedechantiers.personnel

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.material.snackbar.Snackbar
import com.techphone78.gestionnairedechantiers.MainActivity
import com.techphone78.gestionnairedechantiers.R
import com.techphone78.gestionnairedechantiers.databinding.GestionPersonnelFragmentBinding
import com.techphone78.gestionnairedechantiers.utils.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class GestionPersonnelFragment : Fragment() {

    private val PERMISSION_CODE = 1000

    private val viewModel: GestionPersonnelViewModel by navGraphViewModels(R.id.gestionPersonnelNavGraph)


    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val imageFilePath =
                context?.let {
                    result.getUriFilePath(context = it, uniqueName = true).toString()
                }
            val cachePhotoFile = imageFilePath?.let { File(it) }
            cachePhotoFile?.let { saveCroppedImage(it) }
        } else {
            // an error occurred
            val exception = result.error
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = GestionPersonnelFragmentBinding.inflate(inflater)
        binding.viewModel = viewModel
        binding.errorState.viewModel = viewModel
        binding.executePendingBindings()
        binding.lifecycleOwner = this

        viewModel.state.observe(viewLifecycleOwner) {
            binding.vfMain.displayedChild = when (it.status) {
                Status.LOADING -> Flipper.LOADING

                Status.SUCCESS -> Flipper.CONTENT

                Status.ERROR -> {
                    Snackbar.make(
                        binding.mainConstraintLayout,
                        "Impossible de sauvegarder les données, veuillez réessayer",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Flipper.CONTENT
                }
            }
        }

        viewModel.navigationPersonnel.observe(viewLifecycleOwner) { navigation ->
            hideKeyboard(activity as MainActivity)
            when (navigation) {
                GestionPersonnelViewModel.NavigationMenu.ENREGISTREMENT_PERSONNEL -> {
                    viewModel.onBoutonClicked()
                    val action =
                        GestionPersonnelFragmentDirections.actionGestionPersonnelFragmentPop()
                    findNavController().navigate(action)
                }
                GestionPersonnelViewModel.NavigationMenu.AJOUT_PHOTO -> {
                    selectImage()
                    viewModel.onBoutonClicked()
                }
                GestionPersonnelViewModel.NavigationMenu.EN_ATTENTE -> {
                }
                else -> Timber.e("ERROR NAVIGATION GESTIONPERSONNELFRAGMENT $navigation")

            }

        }


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateTypeView(TypeView.LIST)
    }


    private fun selectImage() {

        //if system os is Marshmallow or Above, we need to request runtime permission
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
/*            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(requireContext(), this);
        }*/

            cropImage.launch(
                options {
                    setGuidelines(CropImageView.Guidelines.ON)
                    setAspectRatio(1, 1)
                }
            )

        }

/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
        }*/
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

        Log.i("photoFile", "photofile: $photoFile")
        photoFile?.let {
            file.copyTo(
                it,
                true
            )
        }// This method will be executed once the timer is over
        if (photoFile != null) {
            viewModel.ajoutPathImage(photoFile.absolutePath.toString())
        }
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
            deleteOnExit()
        }
    }


}