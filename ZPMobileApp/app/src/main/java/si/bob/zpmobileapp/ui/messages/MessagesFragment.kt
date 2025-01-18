package si.bob.zpmobileapp.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import si.bob.zpmobileapp.R
import androidx.navigation.fragment.findNavController
import si.bob.zpmobileapp.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.miscFilterButton.isSelected = true
        binding.miscFilterButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        binding.miscFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.extremeFilterButton.isSelected = true
        binding.extremeFilterButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        binding.extremeFilterButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        binding.extremeFilterButton.setOnClickListener {
            toggleCategory(binding.extremeFilterButton)
        }

        binding.miscFilterButton.setOnClickListener {
            toggleCategory(binding.miscFilterButton)
        }

        binding.newButton.setOnClickListener {
            // Use NavController to navigate to NewMessageFragment
            findNavController().navigate(R.id.action_navigation_messages_to_newMessageFragment)
        }

        // PLUS Button Click --> Open NewMessageFragment
        /*binding.newButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.nav_host_fragment_activity_main, NewMessageFragment())
                .addToBackStack(null)
                .commit()
        }*/
    }

    private fun toggleCategory(button: Button) {
        if (button.isSelected) {
            // Deselect the button
            button.isSelected = false
            button.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
        } else {
            // Select the button
            button.isSelected = true
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}