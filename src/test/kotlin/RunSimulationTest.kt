import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.file.shouldHaveExtension
import io.kotest.matchers.shouldNot
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.core.interfaces.Status
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import java.io.File
import it.unibo.alchemist.model.implementations.times.DoubleTime
import java.util.concurrent.TimeUnit

class RunSimulationTest<T : Any?> : StringSpec(
    {
        val listOfSimulationPaths = File(directorySimulation).walk().filter { it.isFile }.toList()

        listOfSimulationPaths shouldNot beEmpty()

        "extension of configuration files should be $simulationExtension" {
            listOfSimulationPaths.forEach{ it.shouldHaveExtension(simulationExtension) }
        }

        "simulations should terminate with no error" {
            listOfSimulationPaths
                .asSequence()
                .map { LoadAlchemist.from(it).getDefault<T, Euclidean2DPosition>().environment }
                .map { Engine(it, DoubleTime(simulationTime)) }
                .onEach {
                    it.play()
                    it.run()
                }
                .forEach {
                    it.waitFor(Status.TERMINATED, 0, TimeUnit.MILLISECONDS)
                    assert(it.error.isEmpty) {
                        "Error in simulation of: ${it.error.get().message.orEmpty()}"
                    }
                }
        }
    }
) {
    companion object {
        const val directorySimulation = "src/main/yaml"
        const val simulationExtension = ".yml"
        const val simulationTime : Double = 10.0
    }
}
