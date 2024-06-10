package devs.org.quizzybharat.Timer

import android.os.CountDownTimer



class QuizTimer(
    private val onTick: (Long) -> Unit,
    private val onFinish: () -> Unit
) : CountDownTimer(20000, 1000) { // 20 seconds with 1 second interval

    override fun onTick(millisUntilFinished: Long) {
        onTick.invoke(millisUntilFinished)
    }

    override fun onFinish() {
        onFinish.invoke()
    }
}
