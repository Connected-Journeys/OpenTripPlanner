package org.opentripplanner.updater.stoptime;

import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import org.opentripplanner.annotation.Component;
import org.opentripplanner.annotation.ServiceType;
import org.opentripplanner.routing.RoutingService;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.standalone.config.updaters.PollingStoptimeUpdaterParameters;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.GtfsRealtimeFuzzyTripMatcher;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Update OTP stop time tables from some (realtime) source
 *
 * Usage example ('rt' name is an example) in file 'Graph.properties':
 *
 * <pre>
 * rt.type = stop-time-updater
 * rt.frequencySec = 60
 * rt.sourceType = gtfs-http
 * rt.url = http://host.tld/path
 * rt.feedId = TA
 * </pre>
 *
 */
@Component(key = "stop-time-updater",type = ServiceType.GraphUpdater,init = PollingStoptimeUpdaterParameters.class)
public class PollingStoptimeUpdater extends PollingGraphUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(PollingStoptimeUpdater.class);

    /**
     * Parent update manager. Is used to execute graph writer runnables.
     */
    private GraphUpdaterManager updaterManager;

    /**
     * Update streamer
     */
    private TripUpdateSource updateSource;

    /**
     * Property to set on the RealtimeDataSnapshotSource
     */
    private Integer logFrequency;

    /**
     * Property to set on the RealtimeDataSnapshotSource
     */
    private Integer maxSnapshotFrequency;

    /**
     * Property to set on the RealtimeDataSnapshotSource
     */
    private final Boolean purgeExpiredData;

    /**
     * Feed id that is used for the trip ids in the TripUpdates
     */
    private final String feedId;

    private final boolean fuzzyTripMatching;

    /**
     * Set only if we should attempt to match the trip_id from other data in TripDescriptor
     */
    private GtfsRealtimeFuzzyTripMatcher fuzzyTripMatcher;

    public PollingStoptimeUpdater(Parameters parameters) {
        super(parameters);
        // Create update streamer from preferences
        feedId = parameters.getFeedId();
        String sourceType = parameters.getSourceConfig().getType();
        if (sourceType != null) {
            if (sourceType.equals("gtfs-http")) {
                updateSource = new GtfsRealtimeHttpTripUpdateSource(parameters);
            } else if (sourceType.equals("gtfs-file")) {
                updateSource = new GtfsRealtimeFileTripUpdateSource(
                    (GtfsRealtimeFileTripUpdateSource.GtfsRealtimeFileTripUpdateSourceParameters) parameters
                );
            }
        }

        // Configure update source
        if (updateSource == null) {
            throw new IllegalArgumentException(
                    "Unknown update streamer source type: " + sourceType);
        }

        // Configure updater FIXME why are the fields objects instead of primitives? this allows null values...
        int logFrequency = parameters.getLogFrequency();
        if (logFrequency >= 0) {
            this.logFrequency = logFrequency;
        }
        int maxSnapshotFrequency = parameters.getMaxSnapshotFrequencyMs();
        if (maxSnapshotFrequency >= 0) {
            this.maxSnapshotFrequency = maxSnapshotFrequency;
        }
        this.purgeExpiredData = parameters.purgeExpiredData();
        this.fuzzyTripMatching = parameters.fuzzyTripMatching();

        LOG.info("Creating stop time updater running every {} seconds : {}", pollingPeriodSeconds, updateSource);
    }

    @Override
    public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
        this.updaterManager = updaterManager;
    }

    @Override
    public void setup(Graph graph) {
        if (fuzzyTripMatching) {
            this.fuzzyTripMatcher = new GtfsRealtimeFuzzyTripMatcher(new RoutingService(graph));
        }

        // Only create a realtime data snapshot source if none exists already
        TimetableSnapshotSource snapshotSource =
            graph.getOrSetupTimetableSnapshotProvider(TimetableSnapshotSource::new);

        // Set properties of realtime data snapshot source
        if (logFrequency != null) {
            snapshotSource.logFrequency = logFrequency;
        }
        if (maxSnapshotFrequency != null) {
            snapshotSource.maxSnapshotFrequency = maxSnapshotFrequency;
        }
        if (purgeExpiredData != null) {
            snapshotSource.purgeExpiredData = purgeExpiredData;
        }
        if (fuzzyTripMatcher != null) {
            snapshotSource.fuzzyTripMatcher = fuzzyTripMatcher;
        }
    }

    /**
     * Repeatedly makes blocking calls to an UpdateStreamer to retrieve new stop time updates, and
     * applies those updates to the graph.
     */
    @Override
    public void runPolling() {
        // Get update lists from update source
        List<TripUpdate> updates = updateSource.getUpdates();
        boolean fullDataset = updateSource.getFullDatasetValueOfLastUpdates();

        if (updates != null) {
            // Handle trip updates via graph writer runnable
            TripUpdateGraphWriterRunnable runnable =
                    new TripUpdateGraphWriterRunnable(fullDataset, updates, feedId);
            updaterManager.execute(runnable);
        }
    }

    @Override
    public void teardown() {
    }

    public String toString() {
        String s = (updateSource == null) ? "NONE" : updateSource.toString();
        return "Streaming stoptime updater with update source = " + s;
    }

    public interface Parameters extends PollingGraphUpdaterParameters {
        String getFeedId();
        int getLogFrequency();
        int getMaxSnapshotFrequencyMs();
        boolean purgeExpiredData();
        boolean fuzzyTripMatching();
    }
}
