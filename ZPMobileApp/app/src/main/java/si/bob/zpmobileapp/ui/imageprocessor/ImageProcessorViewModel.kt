package si.bob.zpmobileapp.ui.imageprocessor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageProcessorViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Choose or take an image to send for processing."
    }
    val text: LiveData<String> = _text
}