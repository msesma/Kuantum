package eu.sesma.kuantum

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import eu.sesma.kuantum.cuanto.JobInteractor
import eu.sesma.kuantum.cuanto.network.IbmProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val provider = IbmProvider()
    private val interactor = JobInteractor(provider)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_connected.setOnClickListener { connect() }

        bt_bell.setOnClickListener { bell() }

        bt_fourier.setOnClickListener { fourier() }
    }

    override fun onDestroy() {
        super.onDestroy()
        interactor.onDestroy()
    }

    private fun connect() {
        tv_connected.setText(R.string.connecting)
        if (!interactor.init(getString(R.string.ibm_api_token))) {
            Timber.d("Cannot contact IBM Quantum Experience: ${interactor.lastError}")
            tv_connected.setText(R.string.disconnected)
            enableButtons(false)
            return
        }
        enableButtons(true)
        tv_connected.setText(R.string.connected)
    }

    private fun bell() {
        GlobalScope.launch { BellUseCase(interactor).run(::console) }
    }

    private fun fourier() {
        GlobalScope.launch { FourierUseCase(interactor).run(::console) }
    }

    private fun enableButtons(enable: Boolean) {
        bt_bell.isEnabled = enable
        bt_fourier.isEnabled = enable
    }

    @SuppressLint("SetTextI18n")
    private fun console(text: String) {
        tv_result.text = "${tv_result.text}$text\n"
    }
}
