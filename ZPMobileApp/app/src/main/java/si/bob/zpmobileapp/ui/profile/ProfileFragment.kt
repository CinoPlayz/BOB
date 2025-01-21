package si.bob.zpmobileapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import si.bob.zpmobileapp.MyApp
import si.bob.zpmobileapp.R
import si.bob.zpmobileapp.databinding.FragmentProfileBinding
import si.bob.zpmobileapp.utils.backendAuth.deregisterPushTokenAndRemoveFromDevice
import si.bob.zpmobileapp.utils.backendAuth.getPushTokenAndSendToMQTT
import si.bob.zpmobileapp.utils.backendAuth.loginUser
import si.bob.zpmobileapp.utils.backendAuth.loginUserTwoFA
import si.bob.zpmobileapp.utils.backendAuth.logoutUserBackend
import si.bob.zpmobileapp.utils.backendAuth.registerUser

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var loginToken: String? = null // Store login token for TFA
    private lateinit var app: MyApp

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        app = requireActivity().application as MyApp

        checkUserCredentials()
        setupUI()

        return root
    }

    private fun checkUserCredentials() {
        val username = app.sharedPrefs.getString(MyApp.USERNAME_KEY, null)
        val token = app.sharedPrefs.getString(MyApp.TOKEN_KEY, null)

        if (!username.isNullOrEmpty() && !token.isNullOrEmpty()) {
            // Both username and token are present --> show the profile screen
            showProfileScreen(app)
        } else {
            // Clear username and token if they are not valid
            app.sharedPrefs.edit().apply {
                remove(MyApp.USERNAME_KEY)
                remove(MyApp.TOKEN_KEY)
                apply()
            }
        }
    }

    private fun setupUI() {

        // Switch to Register view
        binding.registerButtonSwitch.setOnClickListener {
            showRegisterView()
        }

        // Switch to Login view
        binding.loginButtonSwitch.setOnClickListener {
            showLoginView()
        }

        // Login button click listener
        binding.loginButton.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.username_password_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Perform login
            viewLifecycleOwner.lifecycleScope.launch {
                loginUser(
                    app = app,
                    username = username,
                    password = password,
                    onSuccess = { token ->
                        showProfileScreen(app)
                    },
                    onTwoFA = { token ->
                        loginToken = token
                        showTFAScreen()
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                        resetLoginScreen()
                    }
                )
            }
        }

        // TFA button click listener
        binding.tfaButton.setOnClickListener {
            val totp = binding.tfaInput.text.toString().trim()

            if (totp.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.tfa_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (loginToken == null) {
                Toast.makeText(requireContext(), getString(R.string.no_login_token), Toast.LENGTH_SHORT).show()
                resetLoginScreen()
                return@setOnClickListener
            }

            // Perform TFA login
            viewLifecycleOwner.lifecycleScope.launch {
                loginUserTwoFA(
                    app = app,
                    loginToken = loginToken!!,
                    totp = totp,
                    onSuccess = { token ->
                        showProfileScreen(app)
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                        resetLoginScreen()
                    }
                )
            }
        }

        // Register button click listener
        binding.registerButton.setOnClickListener {
            val email = binding.emailInputRegister.text.toString().trim()
            val username = binding.usernameInputRegister.text.toString().trim()
            val password = binding.passwordInputRegister.text.toString().trim()
            val repeatPassword = binding.passwordInputRepeatRegister.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != repeatPassword) {
                Toast.makeText(requireContext(), getString(R.string.passwords_do_not_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the registerUser function
            viewLifecycleOwner.lifecycleScope.launch {
                registerUser(
                    app = app,
                    email = email,
                    username = username,
                    password = password,
                    onSuccess = {
                        Toast.makeText(requireContext(), getString(R.string.registration_successful), Toast.LENGTH_SHORT).show()
                        showLoginView()
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Logout button click listener
        binding.logoutButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val app = requireActivity().application as MyApp
                // Perform logout
                logoutUserBackend(app = app)
                resetLoginScreen()
            }
        }

        // Extreme event notifications switch
        val switch = binding.switchExtremeNotifications // Assuming it's added in your binding

        val notificationToken = app.sharedPrefs.getString(MyApp.NOTIFICATION_TOKEN_KEY, null)
        switch.isChecked = notificationToken != null

        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getPushTokenAndSendToMQTT(app)
            } else {
                deregisterPushTokenAndRemoveFromDevice(app)
            }
        }
    }

    private fun showLoginView() {
        binding.loginForm.visibility = View.VISIBLE
        binding.registerForm.visibility = View.GONE
        binding.tfaForm.visibility = View.GONE
        binding.profileInfo.visibility = View.GONE
    }

    private fun showRegisterView() {
        binding.registerForm.visibility = View.VISIBLE
        binding.loginForm.visibility = View.GONE
        binding.tfaForm.visibility = View.GONE
        binding.profileInfo.visibility = View.GONE
    }

    private fun showProfileScreen(app: MyApp) {
        binding.loginForm.visibility = View.GONE
        binding.tfaForm.visibility = View.GONE
        binding.profileInfo.visibility = View.VISIBLE
        binding.profileWelcomeText.text = getString(R.string.profile_welcome_message, app.sharedPrefs.getString(MyApp.USERNAME_KEY, ""))
    }

    private fun showTFAScreen() {
        binding.loginForm.visibility = View.GONE
        binding.tfaForm.visibility = View.VISIBLE
        binding.profileInfo.visibility = View.GONE
    }

    private fun resetLoginScreen() {
        binding.loginForm.visibility = View.VISIBLE
        binding.tfaForm.visibility = View.GONE
        binding.profileInfo.visibility = View.GONE
        binding.usernameInput.text.clear()
        binding.passwordInput.text.clear()
        binding.tfaInput.text.clear()
        loginToken = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
