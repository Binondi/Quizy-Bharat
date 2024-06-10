package devs.org.quizzybharat.Data

class ActivityData(var title:String, var score:Int,var totalQuestions:Int,var key : String,var time:String,var completedFrom:String,var isCorrect:Boolean) {
    constructor() : this("",0,0,"","","",true)
}