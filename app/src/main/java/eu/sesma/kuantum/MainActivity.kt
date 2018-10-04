package eu.sesma.kuantum

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import eu.sesma.kuantum.cuanto.JobInteractor
import eu.sesma.kuantum.cuanto.model.QAData
import eu.sesma.kuantum.cuanto.network.Either
import eu.sesma.kuantum.cuanto.network.IbmProvider
import eu.sesma.kuantum.experiments.BellExperiment
import eu.sesma.kuantum.experiments.FourierExperiment
import eu.sesma.kuantum.experiments.GhzExperiment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val provider = IbmProvider()
    private val interactor = JobInteractor(provider)

    private val bell = BellExperiment(interactor, ::result)
    private val fourier = FourierExperiment(interactor, ::result)
    private val ghz = GhzExperiment(interactor, ::result)
    private val experiments = listOf(bell, fourier, ghz)

    private var selected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bt_connected.setOnClickListener { connect() }
        bt_run.setOnClickListener { runExperiment() }

        initExperimentSpinner()
    }

    override fun onDestroy() {
        super.onDestroy()
        interactor.onDestroy()
    }

    private fun initExperimentSpinner() {
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, experiments)
        with(sp_experiment) {
            adapter = arrayAdapter
            setSelection(selected)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selected = position
                }
            }
        }
    }

    private fun connect() {
        bt_connected.setText(R.string.connecting)
        if (!interactor.init(getString(R.string.ibm_api_token))) {
            Timber.d("Cannot contact IBM Quantum Experience: ${interactor.lastError}")
            bt_connected.setText(R.string.disconnected)
            enableUx(false)
            return
        }
        enableUx(true)
        bt_connected.setText(R.string.connected)
    }

    private fun runExperiment() {
        enableUx(false)
        console(experiments[selected].qasm.qasm)
        GlobalScope.launch { experiments[selected].run() }
    }

    private fun enableUx(enable: Boolean) {
        bt_run.isEnabled = enable
        sp_device.isEnabled = enable
    }

    @SuppressLint("SetTextI18n")
    private fun console(text: String) {
        runOnUiThread { tv_code.text = "${tv_code.text}$text\n\n" }
    }

    @SuppressLint("SetTextI18n")
    private fun result(result: Either<String, QAData>) {
        when (result) {
            is Either.Left -> runOnUiThread {
                tv_result.text = "${result.v}\n"
                enableUx(result.v != "running") //TODO Create a enum or sealed class of errors
            }
            is Either.Right -> runOnUiThread {
                enableUx(true)
                //TODO show result as a bar graph
                tv_result.text = "${result.v}\n"
            }
        }
    }
}
