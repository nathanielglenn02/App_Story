package com.example.app_story.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.app_story.R
import com.example.app_story.data.UserPreference
import com.example.app_story.databinding.ActivityAddStoryBinding
import com.example.app_story.network.ApiConfig
import com.example.app_story.model.UploadResponse
import kotlinx.coroutines.flow.collect
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var userPreference: UserPreference
    private var selectedImageUri: Uri? = null

    // Registering for camera and gallery result
    private val cameraResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.ivDefaultImage.setImageURI(selectedImageUri)
        }
    }

    private val galleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivDefaultImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UserPreferences
        userPreference = UserPreference.getInstance(this)

        // Handle Camera Button Click
        binding.btnCamera.setOnClickListener {
            selectedImageUri = getTempUriForCamera()
            selectedImageUri?.let {
                cameraResult.launch(it)
            }
        }

        // Handle Gallery Button Click
        binding.btnGallery.setOnClickListener {
            galleryResult.launch("image/*")
        }

        // Handle Upload Button Click
        binding.btnUpload.setOnClickListener {
            val description = binding.etDescription.text.toString().trim()

            if (selectedImageUri == null || description.isEmpty()) {
                Toast.makeText(this, "Gambar dan deskripsi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launchWhenCreated {
                userPreference.getName().collect { name ->
                    if (name != null) {
                        userPreference.getToken().collect { token ->
                            if (token != null) {
                                uploadStory(description, selectedImageUri!!, token)
                            } else {
                                Toast.makeText(this@AddStoryActivity, "User is not logged in", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@AddStoryActivity, "User name is not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun getTempUriForCamera(): Uri? {
        // Membuat file sementara untuk menyimpan gambar
        val file = File(externalCacheDir, "temp_photo.jpg")
        return FileProvider.getUriForFile(
            this,
            "${packageName}.provider",  // Pastikan ini sesuai dengan package name Anda
            file
        )
    }

    private fun uploadStory(description: String, imageUri: Uri, token: String) {
        binding.progressBar.visibility = View.VISIBLE

        // Convert the image URI to a File object
        val imageFile = getFileFromUri(imageUri)
        if (imageFile == null) {
            Toast.makeText(this@AddStoryActivity, "Gagal mengakses file gambar", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        val requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile)
        val photoPart = MultipartBody.Part.createFormData("photo", imageFile.name, requestFile)

        // Create RequestBody for description
        val descriptionRequestBody = RequestBody.create(MediaType.parse("text/plain"), description)

        // Log the request bodies for debugging
        Log.d("UploadStory", "Uploading Story with description: $description, image: ${imageFile.name}")

        // Periksa apakah token valid
        if (token.isEmpty()) {
            Toast.makeText(this@AddStoryActivity, "Token tidak valid", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            return
        }

        // Make the API call to upload the story
        ApiConfig.getApiService().addStory("Bearer $token", descriptionRequestBody, photoPart).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    // Successfully uploaded
                    Toast.makeText(this@AddStoryActivity, "Story uploaded successfully", Toast.LENGTH_SHORT).show()

                    // Memulai ulang HomeActivity setelah upload berhasil
                    val intent = Intent(this@AddStoryActivity, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clear the activity stack
                    startActivity(intent)  // Kembali ke HomeActivity
                    finish()  // Close AddStoryActivity
                } else {
                    // Log error response for debugging
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("UploadStory", "Failed to upload story: $errorMessage")
                    Toast.makeText(this@AddStoryActivity, "Failed to upload story: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                // Log the error and display it
                Log.e("UploadStory", "Error: ${t.message}")
                Toast.makeText(this@AddStoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            // Jika URI berasal dari galeri atau penyimpanan eksternal, kita gunakan ContentResolver untuk mendapatkan file path
            if (uri.scheme == "content") {
                val inputStream = contentResolver.openInputStream(uri)
                val file = File(cacheDir, "temp_image.jpg") // Tempat menyimpan file sementara
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                file.takeIf { it.exists() }
            } else {
                // Jika URI berasal dari file yang dapat diakses langsung, gunakan path URI tersebut
                File(uri.path!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()  // Menangani kemungkinan exception saat mengakses file
            null
        }
    }
}
