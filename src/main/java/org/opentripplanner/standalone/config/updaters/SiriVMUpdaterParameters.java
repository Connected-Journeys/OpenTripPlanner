package org.opentripplanner.standalone.config.updaters;

import org.opentripplanner.ext.siri.updater.SiriVMUpdater;
import org.opentripplanner.standalone.config.NodeAdapter;
import org.opentripplanner.standalone.config.updaters.sources.SiriVMHttpTripUpdaterSourceParameters;

public class SiriVMUpdaterParameters extends PollingGraphUpdaterParameters
    implements SiriVMUpdater.Parameters {

  private final String feedId;
  private final int logFrequency;
  private final int maxSnapshotFrequencyMs;
  private final boolean purgeExpiredData;
  private final boolean fuzzyTripMatching;
  private final boolean blockReadinessUntilInitialized;
  private final SiriVMHttpTripUpdaterSourceParameters source;

  public SiriVMUpdaterParameters(NodeAdapter c) {
    super(c);
    feedId = c.asText("feedId", null);
    logFrequency = c.asInt("logFrequency", -1);
    maxSnapshotFrequencyMs = c.asInt("maxSnapshotFrequencyMs", -1);
    purgeExpiredData = c.asBoolean("purgeExpiredData", false);
    fuzzyTripMatching = c.asBoolean("fuzzyTripMatching", false);
    blockReadinessUntilInitialized = c.asBoolean("blockReadinessUntilInitialized", false);
    source = new SiriVMHttpTripUpdaterSourceParameters(c);
  }


  public String getFeedId() { return feedId; }

  public int getLogFrequency() { return logFrequency; }

  public int getMaxSnapshotFrequencyMs() { return maxSnapshotFrequencyMs; }

  public boolean purgeExpiredData() { return purgeExpiredData; }

  public boolean fuzzyTripMatching() { return fuzzyTripMatching; }

  public boolean blockReadinessUntilInitialized() { return blockReadinessUntilInitialized; }
}
