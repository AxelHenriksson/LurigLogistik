package se.henaxel.luriglogistik

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, findViewById<Game>(R.id.game)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

    }



    override fun onResume() {
        super.onResume()

        (((findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)) as Game).let {
            Log.d("MainActivity", "Game onResume() called for non-null id R.id.game")
            it.onResume()
        } ?: Log.d("MainActivity", "Game onResume() skipped due to null id R.id.game")
    }

    override fun onPause() {
        super.onPause()
        (((findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)) as Game).let {
            Log.d("MainActivity", "Game onPause() called for non-null id R.id.game")
            it.onPause()
        } ?: Log.d("MainActivity", "Game onPause() skipped due to null id R.id.game")
    }
}