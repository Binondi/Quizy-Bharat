package devs.org.quizzybharat.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import devs.org.quizzybharat.Data.ActivityData
import devs.org.quizzybharat.R

class ActivityAdapter(var context: Context, var list :ArrayList<ActivityData>): RecyclerView.Adapter<ActivityAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_items,parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.bind(data)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val score: TextView = itemView.findViewById(R.id.score)
        private val category: TextView = itemView.findViewById(R.id.category)
        private val bg: LinearLayout = itemView.findViewById(R.id.bg)
        private val done: ImageView = itemView.findViewById(R.id.domeImg)

        @SuppressLint("SetTextI18n")
        fun bind(data : ActivityData){

            title.text = data.title
            time.text = data.time
            score.text = "${data.score}/${data.totalQuestions}"
            category.text = "Played from: ${data.completedFrom}"
            if (data.completedFrom== "Reel Challenge"){
                if (data.isCorrect) {
                    bg.setBackgroundResource(R.drawable.green_border_corner)
                    done.setImageResource(R.drawable.done)
                }else{
                    bg.setBackgroundResource(R.drawable.orange_border_corner)
                    done.setImageResource(R.drawable.wrong_icon)
                }
            }else {
                bg.setBackgroundResource(R.drawable.yellow_border_corner)
                done.setImageResource(R.drawable.done_yelow)
            }

            bg.setOnClickListener {

            }

        }
    }
}