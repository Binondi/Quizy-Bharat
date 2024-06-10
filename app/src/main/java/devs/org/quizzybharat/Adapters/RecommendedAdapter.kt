package devs.org.quizzybharat.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import devs.org.quizzybharat.Data.ActivityData
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.PlayQuizActivity
import devs.org.quizzybharat.R
import kotlin.random.Random

class RecommendedAdapter(private val context: Context,
                         private var list: List<QuestionData>,
                         private var list2:List<ActivityData>) :
    RecyclerView.Adapter<RecommendedAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recomended_items, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {



        val data = list[position]
        val data2 = list2
        holder.bind(data, list2)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.image)
        private val question: TextView = itemView.findViewById(R.id.question)
        private val noOfQuizzes: TextView = itemView.findViewById(R.id.noOfQ)
        private val noOfParticipants: TextView = itemView.findViewById(R.id.noOfParticipants)
        private val bg: LinearLayout = itemView.findViewById(R.id.bg)
        private val button: LinearLayout = itemView.findViewById(R.id.button)
        private val btnText: TextView = itemView.findViewById(R.id.btnText)

        @SuppressLint("SetTextI18n")
        fun bind(recomendedData: QuestionData,data2List:List<ActivityData>) {
            val randomInt = Random.nextInt(10, 999)
            question.text = recomendedData.title
            noOfQuizzes.text = "${recomendedData.totalQuestions} Quizzes"
            noOfParticipants.text = "$randomInt Attempts"

            var isCompleted = false
            for (data2 in data2List) {
                if (recomendedData.title == data2.title) {
                    isCompleted = true
                    break
                }
            }
            if (isCompleted){
                button.setBackgroundResource(R.drawable.rounded_corners)
                btnText.text = "Completed"
                btnText.setTextColor(Color.GRAY)
            }else {
                button.setBackgroundResource(R.drawable.green_corner_bg)
                btnText.text = "Play Now"
                btnText.setTextColor(Color.BLACK)
                bg.setOnClickListener {
                    context.startActivity(
                        Intent(context, PlayQuizActivity::class.java)
                            .putExtra("title", recomendedData.title)
                            .putExtra("category", recomendedData.category)
                            .putExtra("key", recomendedData.key)
                            .putExtra("noQuizzes", recomendedData.totalQuestions)
                            .putExtra("question", recomendedData.question)
                    )
                }
            }
        }
    }
}