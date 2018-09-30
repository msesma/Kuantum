package eu.sesma.kuantum

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import eu.sesma.kuantum.cuanto.JobInteractor
import eu.sesma.kuantum.cuanto.network.IbmProvider
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val provider = IbmProvider()
    private val interactor = JobInteractor(provider)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!interactor.init(getString(R.string.ibm_api_token))) {
            Timber.d("Cannot contact IBM Quantum Experience: ${interactor.lastError}")
            return
        }

        GlobalScope.launch { BellUseCase(interactor).run() }

        GlobalScope.launch { FourierUseCase(interactor).run() }
    }

    override fun onDestroy() {
        super.onDestroy()
        interactor.onDestroy()
    }
}
