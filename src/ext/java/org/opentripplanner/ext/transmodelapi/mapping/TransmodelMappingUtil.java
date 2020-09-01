package org.opentripplanner.ext.transmodelapi.mapping;

import org.opentripplanner.ext.transmodelapi.model.stop.PlaceType;
import org.opentripplanner.ext.transmodelapi.model.TransmodelPlaceType;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.TransitEntity;
import org.opentripplanner.model.calendar.ServiceDate;

import java.time.LocalDate;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility methods for mapping transmddel API values to and from internal formats.
 */
public class TransmodelMappingUtil {

    private final TimeZone timeZone;

    public TransmodelMappingUtil(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Long serviceDateToSecondsSinceEpoch(ServiceDate serviceDate) {
        if (serviceDate == null) {
            return null;
        }

        return LocalDate.of(serviceDate.getYear(), serviceDate.getMonth(), serviceDate.getDay())
                .atStartOfDay(timeZone.toZoneId()).toEpochSecond();
    }

    public ServiceDate secondsSinceEpochToServiceDate(Long secondsSinceEpoch) {
        if (secondsSinceEpoch == null) {
            return new ServiceDate();
        }
        return new ServiceDate(new Date(secondsSinceEpoch * 1000));
    }


    // public List<PlaceType> mapPlaceTypes(List<TransmodelPlaceType> inputTypes) {
    //     if (inputTypes == null) {
    //         return null;
    //     }
    //
    //     return inputTypes.stream().map(pt -> mapPlaceType(pt)).distinct().collect(Collectors.toList());
    // }

    private PlaceType mapPlaceType(TransmodelPlaceType transmodelType){
        if (transmodelType!=null) {
            switch (transmodelType) {
                case QUAY:
                case STOP_PLACE:
                    return PlaceType.STOP;
                case BICYCLE_RENT:
                    return PlaceType.BICYCLE_RENT;
                case BIKE_PARK:
                    return PlaceType.BIKE_PARK;
                case CAR_PARK:
                    return PlaceType.CAR_PARK;
            }
        }
        return null;
    }
}
