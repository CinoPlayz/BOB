package si.bob.zpmobileapp.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.json.JSONObject
import si.bob.zpmobileapp.MyApp
import si.bob.zpmobileapp.R
import si.bob.zpmobileapp.databinding.FragmentNewMessageBinding
import java.time.format.DateTimeFormatter

class NewMessageFragment : Fragment() {

    private var _binding: FragmentNewMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var app: MyApp
    private lateinit var token: String
    private lateinit var username: String
    private var category: String = Category.MISCELLANEOUS.displayName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewMessageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        app = requireActivity().application as MyApp
        token = app.sharedPrefs.getString(MyApp.TOKEN_KEY, "") ?: ""
        username = app.sharedPrefs.getString(MyApp.USERNAME_KEY, "") ?: ""

        // Hide the back button in the action bar
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Set "Misc" as selected by default
        binding.miscSelectButton.isSelected = true
        binding.miscSelectButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        binding.miscSelectButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.extremeSelectButton.isSelected = false
        binding.extremeSelectButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.extremeSelectButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))


        // Set click listeners to handle button selection/deselection
        binding.extremeSelectButton.setOnClickListener {
            toggleButtonSelection(binding.extremeSelectButton, binding.miscSelectButton)
            category = Category.EXTREME.displayName
        }

        binding.miscSelectButton.setOnClickListener {
            toggleButtonSelection(binding.miscSelectButton, binding.extremeSelectButton)
            category = Category.MISCELLANEOUS.displayName
        }

        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString()
            if (messageText.isNotBlank()) {
                sendMessageToServer(messageText)
            } else {
                Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    private fun sendMessageToServer(message: String) {

        val mqttClient = (requireActivity().application as MyApp).mqttClient

        if (mqttClient == null || !mqttClient.isConnected) {
            Toast.makeText(requireContext(), "MQTT client not connected", Toast.LENGTH_SHORT).show()
            return
        }

        val timeOfMessage = DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())

        val requestPayload = JSONObject().apply {
            put("token", token)
            put("username", username)
            put("timeOfMessage", timeOfMessage)
            put("message", message)
            put("category", category)
        }

        val requestTopic = "app/messages/create/request"
        val responseTopic = "app/messages/create/response/$username"

        mqttClient.subscribe(responseTopic)

        mqttClient.publish(requestTopic, MqttMessage(requestPayload.toString().toByteArray()))

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                requireActivity().runOnUiThread {
                    // Toast.makeText(requireContext(), "MQTT connection lost: ${cause?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT", "Message arrived on topic: $topic")
                if (topic == responseTopic) {
                    val response = message?.toString()
                    val jsonResponse = JSONObject(response ?: "{}")
                    val success = jsonResponse.optBoolean("success", false)

                    requireActivity().runOnUiThread {
                        if (success) {
                            Toast.makeText(context, "Message sent.", Toast.LENGTH_SHORT).show()
                            requireActivity().supportFragmentManager.popBackStack() // Close fragment after sending
                        } else {
                            val errorMessage = jsonResponse.optString("message", "Unknown error")
                            Toast.makeText(requireContext(), "Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // Optional: Handle delivery confirmation
            }
        })
    }

    private fun toggleButtonSelection(selected: Button, deselected: Button) {
        // Mark the selected button as active
        selected.isSelected = true
        selected.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        // Mark the other button as inactive
        deselected.isSelected = false
        deselected.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        deselected.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
