package devs.org.quizzybharat.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import devs.org.quizzybharat.R

class DialogRecyclerAdapter(
    var context: Context,
    private var list: List<String>
) : RecyclerView.Adapter<DialogRecyclerAdapter.ViewHolder>() {

    val selectedItems = mutableListOf<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DialogRecyclerAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_recycler_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DialogRecyclerAdapter.ViewHolder, position: Int) {
        val categories = list[position]
        holder.chackBox.text = categories
        holder.chackBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(categories)
            } else {
                selectedItems.remove(categories)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chackBox: CheckBox = itemView.findViewById(R.id.checkBox)
    }
}
