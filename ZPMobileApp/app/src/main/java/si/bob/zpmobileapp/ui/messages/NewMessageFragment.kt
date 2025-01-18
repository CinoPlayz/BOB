package si.bob.zpmobileapp.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import si.bob.zpmobileapp.R
import si.bob.zpmobileapp.databinding.FragmentNewMessageBinding

class NewMessageFragment : Fragment() {

    private var _binding: FragmentNewMessageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewMessageBinding.inflate(inflater, container, false)
        val root: View = binding.root

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
        }

        binding.miscSelectButton.setOnClickListener {
            toggleButtonSelection(binding.miscSelectButton, binding.extremeSelectButton)
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
        // TODO: Implement network call to send the message to the server
        Toast.makeText(context, "Message sent: $message", Toast.LENGTH_SHORT).show()
        requireActivity().supportFragmentManager.popBackStack() // Close fragment after sending
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
