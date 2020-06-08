package org.opentripplanner.transit.raptor.rangeraptor.standard.stoparrivals.view;

import org.opentripplanner.transit.raptor.api.transit.RaptorTransfer;
import org.opentripplanner.transit.raptor.api.transit.RaptorTripSchedule;
import org.opentripplanner.transit.raptor.api.view.AccessLegView;
import org.opentripplanner.transit.raptor.api.view.ArrivalView;

final class Access<T extends RaptorTripSchedule>
    extends StopArrivalViewAdapter<T>
    implements AccessLegView
{
    private final int arrivalTime;
    private final RaptorTransfer access;

    Access(int stop, int arrivalTime, RaptorTransfer access) {
        super(0, stop);
        this.arrivalTime = arrivalTime;
        this.access = access;
    }

    @Override
    public int arrivalTime() {
        return arrivalTime;
    }

    @Override
    public boolean arrivedByAccessLeg() {
        return true;
    }

    @Override
    public AccessLegView accessLeg() {
        return this;
    }

    @Override
    public RaptorTransfer access() {
        return access;
    }

    @Override
    public ArrivalView<T> previous() {
        throw new UnsupportedOperationException("Access arrival is the first leg.");
    }
}
