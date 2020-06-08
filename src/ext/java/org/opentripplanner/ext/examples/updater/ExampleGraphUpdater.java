package org.opentripplanner.ext.examples.updater;

import org.opentripplanner.annotation.Component;
import org.opentripplanner.annotation.ServiceType;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.standalone.config.updaters.PollingGraphUpdaterParameters;
import org.opentripplanner.updater.GraphUpdater;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.opentripplanner.updater.GraphUpdaterConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class shows an example of how to implement a graph updater. Besides implementing the methods
 * of the interface {@link GraphUpdater}, the updater also needs to be registered in
 * 'router-config.json'.
 * <p>
 * See the configuration documentation.
 * <p>
 * This example is suited for streaming updaters. For polling updaters it is better to use the
 * abstract base class PollingGraphUpdater. The class ExamplePollingGraphUpdater shows an example of
 * this.
 * <p>
 * Usage example in the file 'router-config.json':
 * <pre>
 * {
 *    "type": "example-updater",
 *    "frequencySec": 60,
 *    "url": "https://api.updater.com/example-updater"
 * }
 * </pre>
 *
 * @see ExamplePollingGraphUpdater
 * @see GraphUpdaterConfigurator
 */
@Component(key = "example-updater", type = ServiceType.GraphUpdater, init = PollingGraphUpdaterParameters.class)
public class ExampleGraphUpdater implements GraphUpdater {

    private static Logger LOG = LoggerFactory.getLogger(ExampleGraphUpdater.class);

    private GraphUpdaterManager updaterManager;

    private Integer frequencySec;

    private String url;

    // Here the updater can be configured using the properties in the file 'Graph.properties'.
    public ExampleGraphUpdater(PollingGraphUpdater.PollingGraphUpdaterParameters config) {
        frequencySec = config.getFrequencySec();
        url = config.getUrl();
        LOG.info("Configured example updater: frequencySec={} and url={}", frequencySec, url);
    }

    // Here the updater gets to know its parent manager to execute GraphWriterRunnables.
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        LOG.info("Example updater: updater manager is set");
        this.updaterManager = updaterManager;
    }

    // Here the updater can be initialized.
    public void setup(Graph graph) {
        LOG.info("Setup example updater");
    }

    // This is where the updater thread receives updates and applies them to the graph.
    // This method only runs once.
    public void run() {
        LOG.info("Run example updater with hashcode: {}", this.hashCode());
        // Here the updater can connect to a server and register a callback function
        // to handle updates to the graph
    }

    // Here the updater can cleanup after itself.
    public void teardown() {
        LOG.info("Teardown example updater");
    }

    public String getName() {
        return "ExampleGraphUpdater";
    }
}
