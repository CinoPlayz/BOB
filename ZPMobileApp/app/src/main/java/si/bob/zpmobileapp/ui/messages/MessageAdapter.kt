package si.bob.zpmobileapp.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import si.bob.zpmobileapp.R
import si.bob.zpmobileapp.databinding.ItemMessageBinding

class MessageAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.usernameTextView.text = message.postedByUser
            binding.timeTextView.text = message.timeOfMessage
            binding.messageTextView.text = message.message

            when (message.category) {
                Category.EXTREME -> {
                    binding.root.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.extreme_color))
                }
                Category.MISCELLANEOUS -> {
                    binding.root.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.miscellaneous_color))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}
