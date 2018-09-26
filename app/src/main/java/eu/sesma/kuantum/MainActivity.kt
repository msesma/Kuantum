package eu.sesma.kuantum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import eu.sesma.kuantum.cuanto.network.IbmProvider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Bell(getString(R.string.ibm_api_token), IbmProvider()).run()
    }
}
