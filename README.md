

# Kuantum

Run quantum experiments writen in Quantum Assembler (QASM) in the IBM Quantum Experience real devices or simulators.

### Api Key

You need an API key. goto https://quantumexperience.ng.bluemix.net/qx/experience and create an account. Under *My Account* you can create an API Key

Add your API token to your system gradle.properties file: `IBM_api_token=""jdhlaskdjhfglasjkdhgladsjhfgladfhjgvladjkf...."`


Add your own experiments easily:

```Kotlin
class GhzExperiment(interactor: JobInteractor,
                       result: (Either<String, QAData>) -> Unit) : Experiment(interactor, result) {

       override val describe = "GHZ experiment XXY."

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
```


Kuantum está inspirado en la [DSL Qotlin](https://github.com/ssuukk/Qotlin)