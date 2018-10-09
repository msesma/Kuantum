package eu.sesma.kuantum

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import eu.sesma.kuantum.cuanto.JobInteractor
import eu.sesma.kuantum.cuanto.model.QAData
import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.network.Either
import eu.sesma.kuantum.cuanto.network.IbmProvider
import eu.sesma.kuantum.experiments.BellExperiment
import eu.sesma.kuantum.experiments.FourierExperiment
import eu.sesma.kuantum.experiments.GhzExperiment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.Main
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
    protected lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val provider = IbmProvider()
    private val interactor = JobInteractor(provider)
    private val graph = Graph()

    private val bell = BellExperiment(interactor, ::result)
    private val fourier = FourierExperiment(interactor, ::result)
    private val ghz = GhzExperiment(interactor, ::result)
    private val experiments = listOf(bell, fourier, ghz)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        job = Job()

        // im_result.visibility= VISIBLE
        // tv_result.visibility=INVISIBLE
        // bt_connected.setOnClickListener { graph.drawResult(im_result, QAData(counts = mapOf(Pair("00000", 300), Pair("00001", 700)))) }
        bt_connected.setOnClickListener { connect() }
        bt_run.setOnClickListener { runExperiment() }
        tv_code.movementMethod = ScrollingMovementMethod()

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
            setSelection(0)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    console(experiments[position].qasm.qasm)
                }
            }
        }
    }

    private fun initDeviceSpinner(devices: List<QADevice>) {
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, devices)
        with(sp_device) {
            adapter = arrayAdapter
            setSelection(devices.indexOf(devices.first { it.simulator }))
        }
    }

    private fun connect() {
        bt_connected.setText(R.string.connecting)
        if (!interactor.init(getString(R.string.ibm_api_token))) {
            Timber.d("Cannot contact IBM Quantum Experience: ${interactor.lastError}")
            bt_connected.setText(R.string.disconnected)
            initDeviceSpinner(emptyList())
            enableUx(false)
            return
        }
        initDeviceSpinner(interactor.devices)
        enableUx(true)
        bt_connected.setText(R.string.connected)

    }

    private fun runExperiment() {
        enableUx(false)
        val experiment = experiments[sp_experiment.selectedItemPosition]
        val device = interactor.devices[sp_device.selectedItemPosition]
        experiment.run(device)
//        GlobalScope.launch(Dispatchers.IO) { experiment.run(device) }
    }

    private fun enableUx(enable: Boolean) {
        bt_run.isEnabled = enable
        sp_device.isEnabled = enable
    }

    @SuppressLint("SetTextI18n")
    private fun console(text: String) {
        tv_code.text = "$text\n\n"
    }

    @SuppressLint("SetTextI18n")
    private fun result(result: Either<String, QAData>) {
        Timber.d("Result outside ${Thread.currentThread()}")
        launch(Dispatchers.Main) {
            Timber.d("Result inside ${Thread.currentThread()}")
            when (result) {
                is Either.Left -> {
                    im_result.visibility = INVISIBLE
                    tv_result.visibility = VISIBLE
                    tv_result.text = "${result.v}\n"
                    enableUx(result.v != "running") //TODO Create a enum or sealed class of errors
                }
                is Either.Right -> {
                    enableUx(true)
                    graph.drawResult(im_result, result.v)
                    im_result.visibility = VISIBLE
                    tv_result.visibility = INVISIBLE
//                    tv_result.text = "${result.v}\n"
                }
            }
        }
    }
}
