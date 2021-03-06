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
import arrow.core.Either
import eu.sesma.kuantum.cuanto.JobInteractor
import eu.sesma.kuantum.cuanto.model.QAData
import eu.sesma.kuantum.cuanto.model.QADevice
import eu.sesma.kuantum.cuanto.network.IbmProvider
import eu.sesma.kuantum.experiments.BellExperiment
import eu.sesma.kuantum.experiments.FourierExperiment
import eu.sesma.kuantum.experiments.GhzExperiment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val provider = IbmProvider()
    private val interactor = JobInteractor(provider)
    private val graph = Graph()

    private val bell = BellExperiment(interactor, ::result)
    private val fourier = FourierExperiment(interactor, ::result)
    private val ghz = GhzExperiment(interactor, ::result)
    private val experiments = listOf(bell, fourier, ghz)

    private var lastData: QAData? = null
    private var lastExperiment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        job = Job()

        setGraphDimension()

//        lastData = QAData(counts = mapOf(Pair("00000", 300), Pair("00001", 700)))//Test
        enableUx(false)
        fab.hide()

        bt_connected.setOnClickListener { connect() }
        bt_run.setOnClickListener { runExperiment() }
        fab.setOnClickListener { showHistogram() }
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
                    if (position == lastExperiment) return
                    lastExperiment = position
                    tv_result.text = ""
                    lastData = null
                    fab.hide()
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
        bt_connected.isEnabled = false
        bt_connected.setText(R.string.connecting)
        launch(Dispatchers.IO) {
            if (!interactor.init(getString(R.string.ibm_api_token))) {
                Timber.d("Cannot contact IBM Quantum Experience: ${interactor.lastError}")
                bt_connected.setText(R.string.disconnected)
                initDeviceSpinner(emptyList())
                enableUx(false)
                bt_connected.isEnabled = true
                return@launch
            }
            withContext(Dispatchers.Main) {
                initDeviceSpinner(interactor.devices)
                enableUx(true)
                bt_connected.setText(R.string.connected)
            }
        }
    }

    private fun runExperiment() {
        enableUx(false)
        val experiment = experiments[sp_experiment.selectedItemPosition]
        val device = interactor.devices[sp_device.selectedItemPosition]
        launch(Dispatchers.IO) { experiment.run(device) }
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
            result.fold({ error ->
                bar_result.visibility = INVISIBLE
                tv_result.text = "$error\n"
                enableUx(error != "Running") //TODO Create a enum or sealed class of errors
            }, { data ->
                enableUx(true)
                fab.show()
                tv_result.text = "$data\n"
                lastData = data
            })
        }
    }

    private fun showHistogram() {
        val data = lastData ?: return
        bar_result.visibility = if (bar_result.visibility == INVISIBLE) {
            graph.drawResult(bar_result, data)
            fab.rotation = 90F
            VISIBLE
        } else {
            fab.rotation = 0F
            INVISIBLE
        }
    }

    private fun setGraphDimension() {
        bar_result.onGlobalLayout {
            with(bar_result) {
                val params = layoutParams
                params.width = height
                params.height = width
                layoutParams = params
                translationY = -height / 2f
            }
        }
    }
}
