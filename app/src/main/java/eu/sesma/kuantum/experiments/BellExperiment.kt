package eu.sesma.kuantum.experiments

import eu.sesma.kuantum.cuanto.*
import timber.log.Timber

class BellExperiment(private val interactor: JobInteractor,
                     private val console: (String) -> Unit) {

    fun run() {
        val device = interactor.simulator

        Timber.d("Running Bell state experiment.")
        val qasm = qasm {
            qreg(2)         //qreg q[2];
            creg(5)         //creg c[2];
            h(0)            //h q[0];
            cx(0, 1)        //cx q[0],q[1];
            measure(0, 1)   //measure q[0] -> c[0];
            measure(1, 1)   //measure q[1] -> c[1];
        }
        val jobBell = interactor.submitJob(
                device = device,
                shots = 256,
                maxCredits = 1,
                sources = *arrayOf(qasm))
        console(jobBell.toString())

        interactor.onStatus(jobBell, 60,
                { finishedJob ->
                    finishedJob.qasms?.forEach { qasm ->
                        Timber.d(qasm.result.toString())
                        console(qasm.result.toString())
                    }
                }, { error -> Timber.d(error) }
        )
    }
}