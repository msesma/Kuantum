package eu.sesma.kuantum.experiments

import eu.sesma.kuantum.cuanto.*
import timber.log.Timber

class GhzExperiment(private val interactor: JobInteractor,
                    private val console: (String) -> Unit) {

    fun run() {
        val device = interactor.simulator

        Timber.d("Running GHZ experiment XXY.")
        val qasm = qasm {
            qreg(3)         //qreg q[3];
            creg(5)         //creg c[5];

            x(0)            // x q[0];
            h(1)            // h q[1];
            h(2)            // h q[2];
            cx(1,0)         // cx q[1],q[0];
            cx(2,0)         // cx q[2],q[0];
            h(0)            // h q[0];
            h(1)            // h q[1];
            h(2)            // h q[2];
            barrier()       // barrier q[0],q[1],q[2],q[3],q[4];
            sdg(2)          // sdg q[2];
            h(0)            // h q[0];
            h(1)            // h q[1];
            h(2)            // h q[2];
            measure(0, 0)   //measure q[0] -> c[0];
            measure(1, 1)   //measure q[1] -> c[1];
            measure(2, 2)   //measure q[2] -> c[2];
        }
        val jobBell = interactor.submitJob(
                device = device,
                shots = 1024,
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