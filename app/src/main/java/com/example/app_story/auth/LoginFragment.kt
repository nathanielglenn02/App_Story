package com.example.app_story.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.app_story.R
import com.example.app_story.data.UserPreference
import com.example.app_story.databinding.FragmentLoginBinding
import com.example.app_story.model.LoginResponse
import com.example.app_story.network.ApiConfig
import com.example.app_story.ui.HomeActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
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

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Email dan Password tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Tampilkan ProgressBar
            binding.progressBar.visibility = View.VISIBLE

            // API call untuk login
            ApiConfig.getApiService().login(email, password).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    // Sembunyikan ProgressBar
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null && !loginResponse.error) {
                            val token = loginResponse.loginResult.token

                            lifecycleScope.launch {
                                // Simpan token ke DataStore
                                userPreference.saveToken(token)

                                // Ambil kembali token untuk memastikan tersimpan
                                val savedToken = userPreference.getToken().first()
                                if (savedToken != null) {
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
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Gagal menyimpan token.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
                    // Sembunyikan ProgressBar
                    binding.progressBar.visibility = View.GONE

                    Toast.makeText(
                        requireContext(),
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // Navigate to RegisterFragment
        binding.tvRegisterLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
