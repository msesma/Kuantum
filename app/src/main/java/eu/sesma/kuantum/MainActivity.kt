package eu.sesma.kuantum

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import eu.sesma.kuantum.cuanto.network.IbmGateway

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Bell(getString(R.string.ibm_api_token), IbmGateway()).run()
    }
}
