package si.bob.zpmobileapp.ui.messages

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import si.bob.zpmobileapp.R
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import si.bob.zpmobileapp.MyApp
import si.bob.zpmobileapp.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter

    private lateinit var app: MyApp
    private lateinit var uuid: String
    private lateinit var token: String

    val messageList = mutableListOf<Message>()
    private val selectedCategories = mutableSetOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        app = requireActivity().application as MyApp
        uuid = app.sharedPrefs.getString(MyApp.UUID_KEY, null) ?: ""
        token = app.sharedPrefs.getString(MyApp.TOKEN_KEY, "") ?: ""

        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        checkUserCredentials()

        return root
    }

    private fun checkUserCredentials() {
        val username = app.sharedPrefs.getString(MyApp.USERNAME_KEY, null)
        val token = app.sharedPrefs.getString(MyApp.TOKEN_KEY, null)

        if (!username.isNullOrEmpty() && !token.isNullOrEmpty()) {
            // Both username and token are present --> show the profile screen
            setupUI()
        } else {
            // Clear username and token if they are not valid
            app.sharedPrefs.edit().apply {
                remove(MyApp.USERNAME_KEY)
                remove(MyApp.TOKEN_KEY)
                apply()
            }
            showLoginLayout()
            setupUIMinimal()
        }
    }

    private fun setupUI() {
        binding.miscFilterButton.isSelected = true
        binding.miscFilterButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        binding.miscFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        selectedCategories.add(Category.MISCELLANEOUS)

        binding.extremeFilterButton.isSelected = true
        binding.extremeFilterButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        binding.extremeFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        selectedCategories.add(Category.EXTREME)

        binding.extremeFilterButton.setOnClickListener {
            toggleCategory(binding.extremeFilterButton, Category.EXTREME)
        }

        binding.miscFilterButton.setOnClickListener {
            toggleCategory(binding.miscFilterButton, Category.MISCELLANEOUS)
        }

        binding.newButton.setOnClickListener {
            // NavController to navigate to NewMessageFragment
            findNavController().navigate(R.id.action_navigation_messages_to_newMessageFragment)
        }

        getMessages()
        startMessagePolling()
    }

    private fun startMessagePolling() {
        // Check if the fragment is attached before starting background work
        if (isAdded) {
            // Perform background tasks or UI updates
            getMessages()
        }

        // Set up polling for messages every 5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startMessagePolling()
        }, 5000)  // 5000ms = 5 seconds
    }

    private fun getMessages() {
        try {
            val mqttClient = (requireActivity().application as MyApp).mqttClient

            if (mqttClient == null || !mqttClient.isConnected) {
                Toast.makeText(requireContext(), "MQTT client not connected", Toast.LENGTH_SHORT).show()
                return
            }

            val requestPayload = JSONObject().apply {
                put("token", token)
                put("uuid", uuid)
            }

            val requestTopic = "app/messages/retrieve/all/request"
            val responseTopic = "app/messages/retrieve/all/response/$uuid"

            mqttClient.subscribe(responseTopic)

            mqttClient.publish(requestTopic, MqttMessage(requestPayload.toString().toByteArray()))

            mqttClient.setCallback(object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "MQTT connection lost: ${cause?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (topic == responseTopic) {
                        val response = message?.toString()
                        val jsonResponse = JSONObject(response ?: "{}")
                        val success = jsonResponse.optBoolean("success", false)

                        if (success) {
                            requireActivity().runOnUiThread {
                                // Toast.makeText(requireContext(), "Messages fetched successfully", Toast.LENGTH_SHORT).show()
                            }

                            val messagesJsonArray = jsonResponse.optJSONArray("messages")
                            Log.d("Messages", "Fetched messages: $messagesJsonArray")

                            if (messagesJsonArray != null) {
                                val newMessageList = mutableListOf<Message>()

                                for (i in 0 until messagesJsonArray.length()) {
                                    val messageData = messagesJsonArray.getJSONObject(i)
                                    val receivedMessage = Message.fromJson(messageData)
                                    newMessageList.add(receivedMessage)
                                }
                                messageList.clear() // Clear the original list
                                messageList.addAll(newMessageList) // Add the new data
                                Log.d("Messages", "Message List Size: ${messageList.size}")
                            }

                            requireActivity().runOnUiThread {
                                listMessages()
                                filterMessages()
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                //Toast.makeText(requireContext(), "Failed to fetch train data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })
        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            Log.e("Messages", "Error", e)
        }
    }

    private fun listMessages() {
        // List like messaging apps - new messages appear at the bottom
        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.stackFromEnd = true

        messageAdapter = MessageAdapter(messageList)
        binding.recyclerView.adapter = messageAdapter

        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.adapter = messageAdapter
    }

    private fun toggleCategory(button: Button, category: Category) {
        if (button.isSelected) {
            // Deselect the button
            button.isSelected = false
            button.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
            selectedCategories.remove(category) // Remove category from selected set
        } else {
            // Select the button
            button.isSelected = true
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            selectedCategories.add(category) // Add category to selected set
        }

        // If no category is selected, select both by default
        if (selectedCategories.isEmpty()) {
            selectedCategories.add(Category.EXTREME)
            selectedCategories.add(Category.MISCELLANEOUS)
            binding.extremeFilterButton.isSelected = true
            binding.miscFilterButton.isSelected = true
            binding.extremeFilterButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
            binding.extremeFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.miscFilterButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
            binding.miscFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        filterMessages()
    }

    private fun filterMessages() {
        val filteredMessages = messageList.filter { message ->
            selectedCategories.contains(message.category)
        }
        messageAdapter = MessageAdapter(filteredMessages)
        binding.recyclerView.adapter = messageAdapter
    }

    private fun setupUIMinimal() {
        binding.navigateToLoginButton.setOnClickListener {
            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.selectedItemId = R.id.navigation_profile
        }
    }

    private fun showLoginLayout() {
        binding.layoutMessages.visibility = View.GONE
        binding.layoutLogin.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}