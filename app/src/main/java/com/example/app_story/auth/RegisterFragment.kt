package com.example.app_story.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.app_story.R
import com.example.app_story.databinding.FragmentRegisterBinding
import com.example.app_story.model.RegisterResponse
import com.example.app_story.network.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle Register Button Click
        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString().trim()
            val email = binding.edRegisterEmail.text.toString().trim()
            val password = binding.edRegisterPassword.text.toString().trim()

            // Validasi input sebelum registrasi
            if (validateInput(name, email, password)) {
                showLoading(true) // Tampilkan ProgressBar
                performRegistration(name, email, password)
            }
        }

        // Navigate back to LoginFragment
        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        var isValid = true

        // Validasi nama
        if (name.isEmpty()) {
            binding.edRegisterName.error = "Nama tidak boleh kosong"
            isValid = false
        }

        // Validasi email menggunakan error dari CustomEmailEditText
        if (email.isEmpty()) {
            binding.edRegisterEmail.error = "Email tidak boleh kosong"
            isValid = false
        } else if (binding.edRegisterEmail.error != null) {
            isValid = false // Error sudah ditampilkan oleh CustomEmailEditText
        }

        // Validasi password
        if (password.isEmpty()) {
            binding.edRegisterPassword.error = "Password tidak boleh kosong"
            isValid = false
        } else if (password.length < 8) {
            binding.edRegisterPassword.error = "Password minimal 8 karakter"
            isValid = false
        }

        return isValid
    }

    private fun performRegistration(name: String, email: String, password: String) {
        ApiConfig.getApiService().register(name, email, password).enqueue(object :
            Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                showLoading(false) // Sembunyikan ProgressBar
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse != null && !registerResponse.error) {
                        Toast.makeText(context, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    } else {
                        Toast.makeText(
                            context,
                            "Registrasi gagal: ${registerResponse?.message ?: "Unknown error"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Terjadi kesalahan: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                showLoading(false) // Sembunyikan ProgressBar
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false // Nonaktifkan tombol selama proses
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true // Aktifkan kembali tombol
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
