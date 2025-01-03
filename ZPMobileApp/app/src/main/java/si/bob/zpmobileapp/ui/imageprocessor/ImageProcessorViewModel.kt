package si.bob.zpmobileapp.ui.imageprocessor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageProcessorViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Image Processor Fragment"
    }
    val text: LiveData<String> = _text
}