package com.example.app_story.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app_story.data.UserPreference
import com.example.app_story.databinding.FragmentLoginBinding
import com.example.app_story.model.LoginResponse
import com.example.app_story.network.ApiConfig
import com.example.app_story.ui.HomeActivity
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var userPreference: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi UserPreference
        userPreference = UserPreference.getInstance(requireContext())

        // Handle Login Button Click
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (validateInput(email, password)) {
                showLoading(true) // Tampilkan ProgressBar dan nonaktifkan tombol
                performLogin(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.edLoginEmail.error = "Email tidak boleh kosong"
            isValid = false
        } else if (binding.edLoginEmail.error != null) {
            isValid = false // Error dari CustomEmailEditText
        }

        if (password.isEmpty()) {
            binding.edLoginPassword.error = "Password tidak boleh kosong"
            isValid = false
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        ApiConfig.getApiService().login(email, password).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                showLoading(false) // Sembunyikan ProgressBar dan aktifkan tombol
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && !loginResponse.error) {
                        val token = loginResponse.loginResult.token
                        val name = loginResponse.loginResult.name

                        lifecycleScope.launch {
                            userPreference.saveUserData(token, name)

                            Toast.makeText(
                                requireContext(),
                                "Login berhasil!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Pindah ke HomeActivity
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Login gagal: ${loginResponse?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Terjadi kesalahan: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                showLoading(false) // Sembunyikan ProgressBar dan aktifkan tombol
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false // Nonaktifkan tombol login
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true // Aktifkan tombol login
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
