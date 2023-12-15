package com.teamlitiaina.tragohelper.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.teamlitiaina.tragohelper.R
import com.teamlitiaina.tragohelper.data.LocationData
import com.teamlitiaina.tragohelper.data.ServiceProviderData
import com.teamlitiaina.tragohelper.data.UserData
import com.teamlitiaina.tragohelper.databinding.ActivityEditInformationBinding
import com.teamlitiaina.tragohelper.dialog.LoadingDialog
import com.teamlitiaina.tragohelper.dialog.UploadingDialog
import com.teamlitiaina.tragohelper.firebase.FirebaseBackend
import com.teamlitiaina.tragohelper.validation.Validation

class EditInformationActivity : AppCompatActivity(), FirebaseBackend.Companion.FirebaseCallback {

    private lateinit var binding: ActivityEditInformationBinding
    private var uri: Uri? = null
    private var imageURL: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInformationBinding.inflate(layoutInflater)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        setContentView(binding.root)

        FirebaseBackend.retrieveData("vehicleOwner", this)

        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                uri = data?.data!!
                binding.profilePictureImageView.setImageURI(uri)
                saveToFirebaseStorage()
            } else {
                Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.uploadProfilePictureButton.setOnClickListener {
            val photoPicker = Intent(Intent.ACTION_PICK)
            photoPicker.type = "image/*"
            activityResultLauncher.launch(photoPicker)
        }

        binding.updateButton.setOnClickListener {
            if (binding.nameEditText.text.toString() == "" || binding.contactNumberEditText.text.toString() == "" || Validation.phoneNumberNotValid(this@EditInformationActivity, binding.contactNumberEditText.text.toString(), "Invalid number format")) {
                Toast.makeText(this@EditInformationActivity, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Update Information")
                builder.setMessage("Information will be changed to\nName: ${binding.nameEditText.text}.\nPhone: ${binding.contactNumberEditText.text}.")
                builder.setPositiveButton("Update") { _, _ ->
                    updatePersonalInformation()
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            }
        }

        binding.backImageButton.setOnClickListener {
            finish()
        }

    }
    private fun saveToFirebaseStorage() {
        val imageReference = FirebaseBackend.storageReference.child("${FirebaseBackend.auth.currentUser?.uid.toString()}/Storage/ProfileImage/${FirebaseBackend.auth.currentUser?.uid.toString()}-profile_image.jpg")
        val uploadTask = imageReference.putFile(uri!!)
        val uploadingDialog = UploadingDialog()
        uploadingDialog.show(supportFragmentManager, "Loading")
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            uploadingDialog.updateProgress(progress)
        }.addOnSuccessListener { taskSnapshot ->
            val uriTask = taskSnapshot.storage.downloadUrl
            if (uriTask.isComplete) {
                val urlImage = uriTask.result
                imageURL = urlImage.toString()
                updatePersonalInformation()
            } else {
                uriTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val urlImage = task.result
                        imageURL = urlImage.toString()
                        val userUpdateMap = mapOf<String, Any>("profilePicture" to imageURL!!)
                        FirebaseBackend.database.getReference("vehicleOwner")
                            .child(FirebaseBackend.auth.currentUser?.uid.toString())
                            .updateChildren(userUpdateMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Profile Picture Updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update profile picture", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.e("FirebaseStorageError", "Error getting download URL", task.exception)
                    }
                }
            }
            uploadingDialog.dismiss()
        }.addOnFailureListener {
            uploadingDialog.dismiss()
            Toast.makeText(this, "Failed to upload", Toast.LENGTH_SHORT).show()
        }
    }
    private fun updatePersonalInformation() {
        val userUpdateMap = mapOf<String, Any>(
            "name" to binding.nameEditText.text.toString(),
            "phoneNumber" to binding.contactNumberEditText.text.toString(),
        )
        FirebaseBackend.database.getReference("vehicleOwner").child(FirebaseBackend.auth.currentUser?.uid.toString()).updateChildren(userUpdateMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile Information Updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update information", Toast.LENGTH_SHORT).show()
            }

    }

    override fun onUserDataReceived(data: UserData) {
        if (!isDestroyed)  {
            binding.nameTextView.text = data.name
            binding.emailTextView.text = data.email
            binding.phoneNumberTextView.text = data.phoneNumber
            binding.nameEditText.setText(data.name)
            binding.contactNumberEditText.setText(data.phoneNumber)
            if (data.profilePicture == "" || data.profilePicture == null) {
                Glide.with(this)
                    .load(R.drawable.temporary_profile_picture)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(binding.profilePictureImageView)
            } else {
                Glide.with(this)
                    .load(data.profilePicture)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(binding.profilePictureImageView)
            }
        }
    }

    override fun onAllUserDataReceived(dataArray: List<UserData>) {}
    override fun onServiceProviderDataReceived(data: ServiceProviderData) {}
    override fun onLocationDataReceived(data: LocationData) {}
    override fun onAllServiceProviderDataReceived(dataArray: List<ServiceProviderData>) {}
    override fun onAllLocationDataReceived(dataArray: List<LocationData>) {}
}