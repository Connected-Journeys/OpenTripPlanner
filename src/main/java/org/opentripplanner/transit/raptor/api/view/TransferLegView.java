package org.opentripplanner.transit.raptor.api.view;

/**
 * Provide transfer leg information to debugger and path mapping.
 */
public interface TransferLegView {

  /** The transfer duration in seconds */
  int durationInSeconds();
}
