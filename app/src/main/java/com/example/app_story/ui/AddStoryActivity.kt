package com.example.app_story.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.app_story.data.UserPreference
import com.example.app_story.databinding.ActivityAddStoryBinding
import com.example.app_story.model.UploadResponse
import com.example.app_story.network.ApiConfig
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        binding.btnCamera.setOnClickListener {
            selectedImageUri = getTempUriForCamera()
            selectedImageUri?.let { uri ->
                cameraResult.launch(uri)
            }
        }

        binding.btnGallery.setOnClickListener {
            galleryResult.launch("image/*")
        }

        binding.btnUpload.setOnClickListener {
            val description = binding.etDescription.text.toString().trim()
            if (selectedImageUri == null || description.isEmpty()) {
                Toast.makeText(this, "Gambar dan deskripsi tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val token = userPreference.getToken().first() ?: ""
                uploadStory(description, token)
            }
        }

        binding.cbAddLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getLocation()
            } else {
                currentLat = null
                currentLon = null
            }
        }
    }

    private val cameraResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            binding.ivDefaultImage.setImageURI(selectedImageUri)
        }
    }

    private val galleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivDefaultImage.setImageURI(it)
        }
    }

    private fun uploadStory(description: String, token: String) {
        binding.progressBar.visibility = View.VISIBLE
        val imageFile = getFileFromUri(selectedImageUri!!)
        val requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile)
        val photoPart = MultipartBody.Part.createFormData("photo", imageFile.name, requestFile)
        val descriptionRequestBody = RequestBody.create(MediaType.parse("text/plain"), description)

        ApiConfig.getApiService().addStory(
            "Bearer $token",
            descriptionRequestBody,
            photoPart,
            currentLat,
            currentLon
        ).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(this@AddStoryActivity, "Story uploaded successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AddStoryActivity, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@AddStoryActivity, "Failed to upload story", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@AddStoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getTempUriForCamera(): Uri {
        val file = File(externalCacheDir, "temp_photo.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )
    }

    private fun getFileFromUri(uri: Uri): File {
        val file = File(cacheDir, "temp_image.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }

    private fun getLocation() {
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Harap aktifkan lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLon = location.longitude
                    Toast.makeText(this, "Lokasi berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    requestNewLocationData()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocation()
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
                currentLat = null
                currentLon = null
            }
        }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    private fun requestNewLocationData() {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        currentLat = location.latitude
                        currentLon = location.longitude
                        Toast.makeText(this@AddStoryActivity, "Lokasi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            },
            mainLooper
        )
    }




}
