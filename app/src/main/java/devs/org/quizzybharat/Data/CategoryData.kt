package devs.org.quizzybharat.Data

class CategoryData(var key: String, var name: String, var noQuizzes: Int, var image: String) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", 0, "")
}
