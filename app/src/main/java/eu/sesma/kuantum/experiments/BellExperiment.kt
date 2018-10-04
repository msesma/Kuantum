package eu.sesma.kuantum.experiments

import eu.sesma.kuantum.cuanto.*
import eu.sesma.kuantum.cuanto.model.QAData

class BellExperiment(interactor: JobInteractor,
                     console: (QAData?) -> Unit) : Experiment(interactor, console) {

    override val describe = "Bell state experiment."

    override val qasm = qasm {
        qreg(2)         //qreg q[2];
        creg(5)         //creg c[2];
        h(0)            //h q[0];
        cx(0, 1)        //cx q[0],q[1];
        measure(0, 1)   //measure q[0] -> c[0];
        measure(1, 1)   //measure q[1] -> c[1];
    }
}