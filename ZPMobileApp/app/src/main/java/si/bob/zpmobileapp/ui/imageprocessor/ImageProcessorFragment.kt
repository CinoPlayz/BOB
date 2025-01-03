package si.bob.zpmobileapp.ui.imageprocessor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import si.bob.zpmobileapp.databinding.FragmentImageprocessorBinding

class ImageProcessorFragment : Fragment() {

    private var _binding: FragmentImageprocessorBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val imageprocessorViewModel =
            ViewModelProvider(this).get(ImageProcessorViewModel::class.java)

        _binding = FragmentImageprocessorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textImageprocessor
        imageprocessorViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}