package devs.org.quizzybharat.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.R

class MyQuizAdapter(var context: Context, var list: List<QuestionData>):RecyclerView.Adapter<MyQuizAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyQuizAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.my_quiz_items,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyQuizAdapter.ViewHolder, position: Int) {
        val data = list[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val question:TextView = itemView.findViewById(R.id.question)
        private val title:TextView = itemView.findViewById(R.id.title)
        private val category:TextView = itemView.findViewById(R.id.category)
        private val views:TextView = itemView.findViewById(R.id.views)
        private val likes:TextView = itemView.findViewById(R.id.likes)
        private val rightAns:TextView = itemView.findViewById(R.id.rightAns)
        private val wrongAns:TextView = itemView.findViewById(R.id.wrongAns)
        private val isApproved:TextView = itemView.findViewById(R.id.isApproved)
        @SuppressLint("SetTextI18n")
        fun bind(data: QuestionData){
            question.text = data.question
            title.text = "Title: ${data.title}"
            category.text = "Category: ${data.category}"
            views.text = "Views: ${data.views}"
            likes.text = "Likes: ${data.like}"
            rightAns.text = "Right Ans: ${data.rightAns}"
            wrongAns.text = "Wrong Ans: ${data.wrongAns}"
            if (data.approved) {
                isApproved.text = "Quiz Approved"
            }else {
                isApproved.text = "Quiz Under Review"
            }
        }
    }

}