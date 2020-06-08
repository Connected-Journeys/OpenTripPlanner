package org.opentripplanner.transit.raptor.rangeraptor.path;

import org.opentripplanner.transit.raptor.api.path.AccessPathLeg;
import org.opentripplanner.transit.raptor.api.path.EgressPathLeg;
import org.opentripplanner.transit.raptor.api.path.Path;
import org.opentripplanner.transit.raptor.api.path.PathLeg;
import org.opentripplanner.transit.raptor.api.path.TransferPathLeg;
import org.opentripplanner.transit.raptor.api.path.TransitPathLeg;
import org.opentripplanner.transit.raptor.api.transit.RaptorCostConverter;
import org.opentripplanner.transit.raptor.api.transit.RaptorTransfer;
import org.opentripplanner.transit.raptor.api.transit.RaptorTripSchedule;
import org.opentripplanner.transit.raptor.api.view.ArrivalView;
import org.opentripplanner.transit.raptor.rangeraptor.WorkerLifeCycle;
import org.opentripplanner.transit.raptor.rangeraptor.transit.TripTimesSearch;


/**
 * Build a path from a destination arrival - this maps between the domain of routing
 * to the domain of result paths. All values not needed for routing is computed as part of this mapping.
 */
public final class ForwardPathMapper<T extends RaptorTripSchedule> implements PathMapper<T> {
    private int iterationDepartureTime = -1;


    public ForwardPathMapper(WorkerLifeCycle lifeCycle) {
        lifeCycle.onSetupIteration(this::setRangeRaptorIterationDepartureTime);
    }

    private void setRangeRaptorIterationDepartureTime(int iterationDepartureTime) {
        this.iterationDepartureTime = iterationDepartureTime;
    }

    @Override
    public Path<T> mapToPath(final DestinationArrival<T> destinationArrival) {
        ArrivalView<T> arrival;
        PathLeg<T> lastLeg;
        TransitPathLeg<T> transitLeg;

        arrival = destinationArrival.previous();
        lastLeg = createEgressPathLeg(destinationArrival);

        do {
            transitLeg = createTransitLeg(arrival, lastLeg);
            arrival = arrival.previous();

            if (arrival.arrivedByTransfer()) {
                lastLeg = createTransferLeg(arrival, transitLeg);
                arrival = arrival.previous();
            }
            else {
                lastLeg = transitLeg;
            }
        }
        while (arrival.arrivedByTransit());

        AccessPathLeg<T> accessLeg = createAccessPathLeg(arrival, transitLeg);

        return new Path<>(iterationDepartureTime, accessLeg, RaptorCostConverter.toOtpDomainCost(destinationArrival.cost()));
    }

    private EgressPathLeg<T> createEgressPathLeg(DestinationArrival<T> destinationArrival) {
        RaptorTransfer egress = destinationArrival.egressLeg().egress();
        int departureTime = destinationArrival.arrivalTime() - egress.durationInSeconds();

        return new EgressPathLeg<>(
            egress,
            destinationArrival.previous().stop(),
            departureTime,
            destinationArrival.arrivalTime()
        );
    }

    private TransitPathLeg<T> createTransitLeg(ArrivalView<T> arrival, PathLeg<T> lastLeg) {
        TripTimesSearch.BoarAlightTimes r = TripTimesSearch.findTripForwardSearch(arrival);

        return new TransitPathLeg<>(
                arrival.previous().stop(),
                r.boardTime,
                arrival.stop(),
                r.alightTime,
                arrival.transitLeg().trip(),
                lastLeg
        );
    }

    private TransferPathLeg<T> createTransferLeg(ArrivalView<T> arrival, TransitPathLeg<T> transitLeg) {
        int departureTime = arrival.arrivalTime() - arrival.transferLeg().durationInSeconds();

        return new TransferPathLeg<>(
                arrival.previous().stop(),
                departureTime,
                arrival.stop(),
                arrival.arrivalTime(),
                transitLeg
        );
    }

    private AccessPathLeg<T> createAccessPathLeg(ArrivalView<T> from, TransitPathLeg<T> nextLeg) {
        RaptorTransfer access = from.accessLeg().access();
        int departureTime = from.arrivalTime() - access.durationInSeconds();

        return new AccessPathLeg<>(
            access,
            from.stop(),
            departureTime,
            from.arrivalTime(),
            nextLeg
        );
    }
}
