package devs.org.quizzybharat.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import devs.org.quizzybharat.Data.ActivityData
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.PlayQuizActivity
import devs.org.quizzybharat.R

class CategoryViewerAdapter(
    var context: Context,
    private var list: List<QuestionData>,
    private var list2:List<ActivityData>)
    : RecyclerView.Adapter<CategoryViewerAdapter.ViewHolder>() {

    private val isCompleted = MutableList(list.size) { false }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewerAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.category_viewer_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewerAdapter.ViewHolder, position: Int) {
        val data = list[position]
        val data2 = list2
        holder.bind(data, data2,position)

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val buttonText: TextView = itemView.findViewById(R.id.buttonText)
        private val noQuestions: TextView = itemView.findViewById(R.id.noQuestions)
        private val bg: LinearLayout = itemView.findViewById(R.id.bg)
        private val button: LinearLayout = itemView.findViewById(R.id.button)
        private val userName: TextView = itemView.findViewById(R.id.username)

        @SuppressLint("SetTextI18n")
        fun bind(innerLst: QuestionData, data2List: List<ActivityData>, position: Int) {
            title.text = innerLst.title
            noQuestions.text = "${innerLst.totalQuestions} Questions"
            userName.text = "Quiz by: ${innerLst.userName}"

            val isQuestionCompleted = data2List.any { it.title == innerLst.title }
            isCompleted[position] = isQuestionCompleted

            if (isCompleted[position]) {
                button.setBackgroundResource(R.drawable.rounded_corners)
                buttonText.text = "Completed"
                bg.isClickable = false
                val typedValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
                val defaultTextColor = ContextCompat.getColor(context, typedValue.resourceId)
                buttonText.setTextColor(defaultTextColor)
            } else if (!isCompleted[position]){
                button.setBackgroundResource(R.drawable.selected)
                buttonText.text = "Play Now"
                buttonText.setTextColor(Color.BLACK)
                bg.isClickable = true
                bg.setOnClickListener {
                    context.startActivity(
                        Intent(context, PlayQuizActivity::class.java)
                            .putExtra("title", innerLst.title)
                            .putExtra("category", innerLst.category)
                            .putExtra("key", innerLst.key)
                            .putExtra("noQuizzes", innerLst.totalQuestions)
                            .putExtra("question", innerLst.question)
                    )
                }
            }

        }


    }

}