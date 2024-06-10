package devs.org.quizzybharat.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import devs.org.quizzybharat.Data.ActivityData
import devs.org.quizzybharat.Data.AddOptions
import devs.org.quizzybharat.Data.QuestionData
import devs.org.quizzybharat.Data.ViewsData
import devs.org.quizzybharat.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask


class ViewPagerAdapter(
    var context: Context,
    private var questionsList: List<QuestionData>,
    private var viewsList: ArrayList<ViewsData>,
    private var t:ArrayList<TimerTask?>,
    private var s :ArrayList<Int>
) : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    private lateinit var coorectAnswer: ArrayList<String>
    private lateinit var options: ArrayList<String>
    private lateinit var listOptions: ArrayList<AddOptions>
    private var map = HashMap<String, Any>()
    private val updateData = FirebaseDatabase.getInstance().reference.child("QuizSet")
    private val viewSystem = FirebaseDatabase.getInstance().reference.child("ViewSystem")
    private val completedData = FirebaseDatabase.getInstance().reference.child("Completed")
    private val user = FirebaseDatabase.getInstance().reference.child("Users")
    private var likes = 0
    private lateinit var isLiked:ArrayList<Boolean>
    private lateinit var c: Calendar
    private val timer = Timer()
    private lateinit var rightAns: MediaPlayer
    private val delay = 1500

    private lateinit var sp: SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context).inflate(R.layout.random_items, parent, false)

        return ViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val questionData = questionsList[position]

        isLiked = ArrayList()

        for (i in questionsList.indices) {
            isLiked.add(false)
        }

        var seconds = s[position]

        holder.updateViews(questionData)

        c = Calendar.getInstance()
        sp = context.getSharedPreferences("details", AppCompatActivity.MODE_PRIVATE)



        var time = t[position]







        coorectAnswer = ArrayList()
        holder.bind(questionData, position)
        holder.addOptionsToList(questionData)
        for (ope in questionsList) {
            coorectAnswer.add(ope.correctOption)
        }

        holder.lottie1.visibility = View.INVISIBLE
        holder.lottie2.visibility = View.INVISIBLE
        holder.lottie3.visibility = View.INVISIBLE
        holder.lottie4.visibility = View.INVISIBLE

        holder.bgOptionOne.setOnClickListener {
            holder.setAllDefault()
            holder.disableButtons()
            t[position]?.cancel()
            if (coorectAnswer[position] == holder.option1.text.toString()) {
                holder.bgOptionOne.setBackgroundResource(R.drawable.green_corner_bg)
                holder.lottie1.visibility = View.VISIBLE
                holder.lottie1.playAnimation()
                rightAns = MediaPlayer.create(context, R.raw.correct)
                rightAns.start()
                holder.submitQuiz(questionData)
                holder.updateRightAns(questionData)
            }
            if (coorectAnswer[position] == holder.option2.text.toString()) {
                holder.bgOptionOne.setBackgroundResource(R.drawable.red_corner_bg)
                holder.bgOptionTwo.setBackgroundResource(R.drawable.green_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
            if (coorectAnswer[position] == holder.option3.text.toString()) {
                holder.bgOptionOne.setBackgroundResource(R.drawable.red_corner_bg)
                holder.bgOptionThree.setBackgroundResource(R.drawable.green_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
            if (coorectAnswer[position] == holder.option4.text.toString()) {
                holder.bgOptionOne.setBackgroundResource(R.drawable.red_corner_bg)
                holder.bgOptionFour.setBackgroundResource(R.drawable.green_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
        }
        holder.bgOptionTwo.setOnClickListener {
            holder.setAllDefault()
            holder.disableButtons()
            t[position]?.cancel()

            if (coorectAnswer[position] == holder.option1.text.toString()) {
                holder.bgOptionTwo.setBackgroundResource(R.drawable.red_corner_bg)
                holder.bgOptionOne.setBackgroundResource(R.drawable.green_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
            if (coorectAnswer[position] == holder.option2.text.toString()) {
                holder.bgOptionTwo.setBackgroundResource(R.drawable.green_corner_bg)
                holder.lottie2.visibility = View.VISIBLE
                holder.lottie2.playAnimation()
                rightAns = MediaPlayer.create(context, R.raw.correct)
                rightAns.start()
                holder.submitQuiz(questionData)
                holder.updateRightAns(questionData)
            }
            if (coorectAnswer[position] == holder.option3.text.toString()) {
                holder.bgOptionThree.setBackgroundResource(R.drawable.green_corner_bg)
                holder.bgOptionTwo.setBackgroundResource(R.drawable.red_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
            if (coorectAnswer[position] == holder.option4.text.toString()) {
                holder.bgOptionFour.setBackgroundResource(R.drawable.green_corner_bg)
                holder.bgOptionTwo.setBackgroundResource(R.drawable.red_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
        }
        holder.bgOptionThree.setOnClickListener {
            holder.setAllDefault()
            holder.disableButtons()
            t[position]?.cancel()
            if (coorectAnswer[position] == holder.option1.text.toString()) {
                holder.bgOptionThree.setBackgroundResource(R.drawable.red_corner_bg)
                holder.bgOptionOne.setBackgroundResource(R.drawable.green_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
            if (coorectAnswer[position] == holder.option2.text.toString()) {
                holder.bgOptionThree.setBackgroundResource(R.drawable.red_corner_bg)
                holder.bgOptionTwo.setBackgroundResource(R.drawable.green_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()

            }
            if (coorectAnswer[position] == holder.option3.text.toString()) {
                holder.bgOptionThree.setBackgroundResource(R.drawable.green_corner_bg)
                holder.lottie3.visibility = View.VISIBLE
                holder.lottie3.playAnimation()
                holder.submitQuiz(questionData)
                holder.updateRightAns(questionData)
                rightAns = MediaPlayer.create(context, R.raw.correct)
                rightAns.start()
            }
            if (coorectAnswer[position] == holder.option4.text.toString()) {
                holder.bgOptionFour.setBackgroundResource(R.drawable.green_corner_bg)
                holder.bgOptionThree.setBackgroundResource(R.drawable.red_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
        }
        holder.bgOptionFour.setOnClickListener {
            holder.setAllDefault()
            holder.disableButtons()
            t[position]?.cancel()
            if (coorectAnswer[position] == holder.option1.text.toString()) {
                holder.bgOptionFour.setBackgroundResource(R.drawable.red_corner_bg)
                holder.bgOptionOne.setBackgroundResource(R.drawable.green_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
            if (coorectAnswer[position] == holder.option2.text.toString()) {
                holder.bgOptionFour.setBackgroundResource(R.drawable.red_corner_bg)
                holder.bgOptionTwo.setBackgroundResource(R.drawable.green_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
            if (coorectAnswer[position] == holder.option3.text.toString()) {
                holder.bgOptionFour.setBackgroundResource(R.drawable.red_corner_bg)
                holder.bgOptionThree.setBackgroundResource(R.drawable.green_corner_bg)
                holder.updateWrongAns(questionData)
                holder.submitWrongQuiz(questionData)
                rightAns = MediaPlayer.create(context, R.raw.wrong)
                rightAns.start()
            }
            if (coorectAnswer[position] == holder.option4.text.toString()) {
                holder.bgOptionFour.setBackgroundResource(R.drawable.green_corner_bg)
                holder.lottie4.visibility = View.VISIBLE
                holder.lottie4.playAnimation()
                holder.submitQuiz(questionData)
                holder.updateRightAns(questionData)
                rightAns = MediaPlayer.create(context, R.raw.correct)
                rightAns.start()
            }
        }


        for (viewData in viewsList) {
            if (viewData.key == questionData.key) {
                isLiked[questionsList.indexOf(questionData)] = true
                holder.like.setImageResource(R.drawable.like_filled)
                break
            }
        }

        holder.like.setOnClickListener {
            if (!isLiked[position]) {
                likes = questionData.like + 1
                holder.like.setImageResource(R.drawable.like_filled)
                holder.updateData(questionData, likes)
                isLiked[position] = true
            }else {
                likes = questionData.like
                holder.like.setImageResource(R.drawable.like_outline)
                isLiked[position] = false

                holder.updateData(questionData, likes)
            }
        }


        holder.threeDot.setOnClickListener {

        }

    }

    override fun getItemCount(): Int {
        return questionsList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<QuestionData>) {
        questionsList = newList
        notifyDataSetChanged()
    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val question: TextView = itemView.findViewById(R.id.question)
        private val uploaderName: TextView = itemView.findViewById(R.id.uploaderName)
        val option1: TextView = itemView.findViewById(R.id.option1)
        val option2: TextView = itemView.findViewById(R.id.option2)
        val option3: TextView = itemView.findViewById(R.id.option3)
        val option4: TextView = itemView.findViewById(R.id.option4)
        private val uploaderProfile: CircleImageView = itemView.findViewById(R.id.uploaderProfile)
        val bgOptionOne: RelativeLayout = itemView.findViewById(R.id.bgOptionOne)
        val bgOptionTwo: RelativeLayout = itemView.findViewById(R.id.bgOptionTwo)
        val bgOptionThree: RelativeLayout = itemView.findViewById(R.id.bgOptionThree)
        val bgOptionFour: RelativeLayout = itemView.findViewById(R.id.bgOptionFour)
        val like: ImageView = itemView.findViewById(R.id.like)
        val threeDot: ImageView = itemView.findViewById(R.id.threeDot)
        private val txtWrong: TextView = itemView.findViewById(R.id.txtWrong)
        private val txtRight: TextView = itemView.findViewById(R.id.txtRight)
        private val txtViews: TextView = itemView.findViewById(R.id.txtViews)
        private val txtLike: TextView = itemView.findViewById(R.id.txtLike)
        val lottie1: LottieAnimationView = itemView.findViewById(R.id.lottie1)
        val lottie2: LottieAnimationView = itemView.findViewById(R.id.lottie2)
        val lottie3: LottieAnimationView = itemView.findViewById(R.id.lottie3)
        val lottie4: LottieAnimationView = itemView.findViewById(R.id.lottie4)

        lateinit var t: TimerTask // Add this line

        fun bind(questionData: QuestionData, position: Int) {

            question.text = questionData.question
            uploaderName.text = questionData.userName
            txtLike.text = questionData.like.toString()
            txtViews.text = questionData.views.toString()
            txtRight.text = questionData.rightAns.toString()
            txtWrong.text = questionData.wrongAns.toString()


            if (isLiked[position]) {
                like.setImageResource(R.drawable.like_filled)
            } else {
                like.setImageResource(R.drawable.like_outline)
            }

            Glide.with(context)
                .load(questionData.imageUrl)
                .placeholder(R.drawable.demo_img)
                .into(uploaderProfile)

            setAllDefault()
        }

        fun setAllDefault() {
            val optionsList = ArrayList<RelativeLayout>()
            optionsList.add(bgOptionOne)
            optionsList.add(bgOptionTwo)
            optionsList.add(bgOptionThree)
            optionsList.add(bgOptionFour)
            for (ope in optionsList) {
                ope.setBackgroundResource(R.drawable.border)
            }

            val textList = ArrayList<TextView>()
            textList.add(option1)
            textList.add(option2)
            textList.add(option3)
            textList.add(option4)
        }

        fun addOptionsToList(questionList: QuestionData) {
            options = ArrayList()
            listOptions = ArrayList()
            options.add(questionList.correctOption)
            options.add(questionList.option2)
            options.add(questionList.option3)
            options.add(questionList.option4)

            options.shuffle()

            val optionCollection = AddOptions(options)

            listOptions.add(optionCollection)

            option1.text = options[0]
            option2.text = options[1]
            option3.text = options[2]
            option4.text = options[3]
        }

        fun disableButtons(){
            bgOptionOne.isClickable = false
            bgOptionTwo.isClickable = false
            bgOptionThree.isClickable = false
            bgOptionFour.isClickable = false
        }
        fun updateData(questionData: QuestionData,lll:Int){
            map.clear()
            map["like"] = lll
            updateData.child(questionData.key).updateChildren(map).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    txtLike.text = likes.toString()
                    isLiked[position] = !isLiked[position]
                    map.clear()
                    map["key"] = questionData.key
                    viewSystem.child(FirebaseAuth.getInstance().uid.toString()).child(questionData.key).updateChildren(map).addOnCompleteListener {

                    }
                }
            }


        }
        @SuppressLint("SetTextI18n")
        fun updateRightAns(questionData: QuestionData){
            map.clear()
            map["rightAns"] = questionData.rightAns + 1
            updateData.child(questionData.key).updateChildren(map).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    map.clear()
                    txtRight.text = "${questionData.rightAns + 1}"

                }
            }
        }
        @SuppressLint("SetTextI18n")
        fun updateWrongAns(questionData: QuestionData){
            map.clear()
            map["wrongAns"] = questionData.wrongAns + 1
            updateData.child(questionData.key).updateChildren(map).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    txtWrong.text = "${questionData.wrongAns + 1}"

                }
            }
        }


        @SuppressLint("SetTextI18n")
        fun updateViews(questionData: QuestionData){

            map.clear()
            map["views"] = questionData.views + 1
            updateData.child(questionData.key).updateChildren(map).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    txtViews.text = "${questionData.views + 1}"

                }
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun submitQuiz(questionData: QuestionData) {
            val pushKey = completedData.push().key
            if (pushKey != null) {
                val value = ActivityData(
                    questionData.question,
                    1,
                    1,
                    pushKey,
                    SimpleDateFormat("dd/MM/yyyy").format(c.time),
                    "Reel Challenge",
                    true
                )
                completedData.child(FirebaseAuth.getInstance().uid.toString()).child(pushKey).setValue(value).addOnCompleteListener { it ->
                    if (it.isSuccessful){

                        if (sp.contains("quizCompleted")) {
                            map["quizCompleted"] = Integer.parseInt(sp.getString("quizCompleted", "0").toString()) + 1
                        }
                        if (sp.contains("rightAnswer")) {
                            map["rightAnswer"] = Integer.parseInt(sp.getString("rightAnswer", "0").toString()) + 1
                        }
                        user.child(FirebaseAuth.getInstance().uid.toString()).updateChildren(map).addOnCompleteListener {
                            if (it.isSuccessful) {

                            }
                        }



                    }
                    else Toast.makeText(context, "Error submitting the quiz, Please try again", Toast.LENGTH_SHORT).show()
                }
            }

        }

        @SuppressLint("SimpleDateFormat")
        fun submitWrongQuiz(questionData: QuestionData) {
            val pushKey = completedData.push().key
            if (pushKey != null) {
                val value = ActivityData(
                    questionData.question,
                    0,
                    1,
                    pushKey,
                    SimpleDateFormat("dd/MM/yyyy").format(c.time),
                    "Reel Challenge",
                    false
                )
                completedData.child(FirebaseAuth.getInstance().uid.toString()).child(pushKey).setValue(value).addOnCompleteListener { it ->
                    if (it.isSuccessful){

                        if (sp.contains("quizCompleted")) {
                            map["quizCompleted"] = Integer.parseInt(sp.getString("quizCompleted", "0").toString()) + 1
                        }
                        if (sp.contains("rightAnswer")) {
                            map["rightAnswer"] = Integer.parseInt(sp.getString("rightAnswer", "0").toString()) + 1
                        }
                        user.child(FirebaseAuth.getInstance().uid.toString()).updateChildren(map).addOnCompleteListener {
                            if (it.isSuccessful) {

                            }
                        }



                    }
                    else Toast.makeText(context, "Error submitting the quiz, Please try again", Toast.LENGTH_SHORT).show()
                }
            }

        }






    }
}