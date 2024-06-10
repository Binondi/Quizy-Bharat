package devs.org.quizzybharat.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import devs.org.quizzybharat.CategoryViewerActivity
import devs.org.quizzybharat.Data.CategoryData
import devs.org.quizzybharat.R

class CategoryAdapter(private val context: Context, private var list: List<CategoryData>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.category_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.image)
        private val category: TextView = itemView.findViewById(R.id.category)
        private val noQuizzes: TextView = itemView.findViewById(R.id.noQuizzes)
        private val lnrCategory: LinearLayout = itemView.findViewById(R.id.lnrCategory)

        fun bind(categoryData: CategoryData) {

            category.text = categoryData.name
            noQuizzes.text = "${categoryData.noQuizzes} Quizzes"

            if (context != null) {
                Glide.with(context)
                    .load(categoryData.image)
                    .placeholder(R.drawable.demo_img)
                    .into(image)
            }
            lnrCategory.setOnClickListener {
                context.startActivity(Intent(context, CategoryViewerActivity::class.java)
                    .putExtra("title", categoryData.name))
            }
        }
    }
}
