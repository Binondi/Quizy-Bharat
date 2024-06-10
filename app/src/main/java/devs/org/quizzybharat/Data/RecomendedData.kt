package devs.org.quizzybharat.Data

class RecomendedData(var key: String, var question: String, var noQuizzes: Int,var noAttempts: Int, var image: String) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", 0,0, "")

}