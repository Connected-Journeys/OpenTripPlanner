package org.opentripplanner.routing.impl;

import java.util.HashMap;
import java.util.Map;

import org.opentripplanner.annotation.Component;
import org.opentripplanner.annotation.ServiceType;
import org.opentripplanner.model.FeedScopedId;
import org.opentripplanner.model.Trip;
import org.opentripplanner.model.OtpTransitService;
import org.opentripplanner.routing.core.FareRuleSet;
import org.opentripplanner.routing.core.StandardFareType;
import org.opentripplanner.routing.services.FareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

@Component(key = "seattle",type = ServiceType.ServiceFactory)
public class SeattleFareServiceFactory extends DefaultFareServiceFactory {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(SeattleFareServiceFactory.class);

    @Override
    public FareService makeFareService() {

    	SeattleFareServiceImpl fareService = new SeattleFareServiceImpl();
    	fareService.addFareRules(StandardFareType.regular, regularFareRules.values());
    	fareService.addFareRules(StandardFareType.youth, regularFareRules.values());
    	fareService.addFareRules(StandardFareType.senior, regularFareRules.values());

    	return fareService;
    }

    @Override
    public void configure(JsonNode config) {
        // No config for the moment
    }

    @Override
    public void processGtfs(OtpTransitService transitService) {
    	// Add custom extension: trips may have a fare ID specified in KCM GTFS.
    	// Need to ensure that we are scoped to feed when adding trips to FareRuleSet,
    	// since fare IDs may not be unique across feeds and trip agency IDsqq
    	// may not match fare attribute agency IDs (which are feed IDs).

    	Map<FeedScopedId, FareRuleSet> feedFareRules = new HashMap<>();
    	fillFareRules(transitService.getAllFareAttributes(),
                transitService.getAllFareRules(), feedFareRules);

    	regularFareRules.putAll(feedFareRules);

    	Map<String, FareRuleSet> feedFareRulesById = new HashMap<>();

        for (FareRuleSet rule : regularFareRules.values()) {
        	String id = rule.getFareAttribute().getId().getId();
        	feedFareRulesById.put(id, rule);
        }

        for (Trip trip : transitService.getAllTrips()) {
        	String fareId = trip.getFareId();
        	FareRuleSet rule = feedFareRulesById.get(fareId);
        	if (rule != null)
        		rule.addTrip(trip.getId());
        }

    }
}
