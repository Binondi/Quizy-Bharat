package devs.org.quizzybharat.Data

class QuestionData(var title: String,var question:String,var correctOption:String,
                   var option2:String,var  option3:String,var option4:String,var approved:Boolean,

var totalQuestions:Int, var userId:String,var key : String,var imageUrl:String,var category:String,var userName:String,var like: Int, var views:Int, var rightAns:Int,var wrongAns:Int) {

    constructor() : this("", "", "","","", "",false,1,"","","","","",0,0,0,0)

}