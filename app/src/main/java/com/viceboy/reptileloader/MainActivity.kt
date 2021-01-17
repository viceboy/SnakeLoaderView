package com.viceboy.reptileloader

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val root = findViewById<ConstraintLayout>(R.id.rootContainer)
        val loader = findViewById<SnakeLoaderView>(R.id.loader)
        val loaderCard = findViewById<SnakeLoaderView>(R.id.loaderCard)
        val loaderCardNew = findViewById<SnakeLoaderView>(R.id.loaderCardNew)

        root.setOnClickListener(object : View.OnClickListener {
            private var flag = false
            override fun onClick(p0: View) {
                flag = if (!flag) {
                    loader.startAnimation{}
                    loaderCard.startAnimation{}
                    loaderCardNew.startAnimation{}
                    true
                } else {
                    loader.stopAnimation{}
                    loaderCard.stopAnimation{}
                    loaderCardNew.stopAnimation{}
                    false
                }
            }
        })
    }
	
	companion object {
	     private const val TAG = "main_active"
	}
}
