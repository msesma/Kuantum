package eu.sesma.kuantum.experiments

import eu.sesma.kuantum.cuanto.*

class GhzExperiment(interactor: JobInteractor,
                    console: (String) -> Unit) : Experiment(interactor, console) {

    override val describe = "Running GHZ experiment XXY."

    override val qasm = qasm {
        qreg(3)         //qreg q[3];
        creg(5)         //creg c[5];

        x(0)            // x q[0];
        h(1)            // h q[1];
        h(2)            // h q[2];
        cx(1, 0)        // cx q[1],q[0];
        cx(2, 0)        // cx q[2],q[0];
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
}