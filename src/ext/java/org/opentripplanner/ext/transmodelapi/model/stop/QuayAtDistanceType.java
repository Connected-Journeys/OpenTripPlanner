package org.opentripplanner.ext.transmodelapi.model.stop;

import graphql.Scalars;
import graphql.relay.Relay;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import org.opentripplanner.ext.transmodelapi.mapping.TransitIdMapper;
import org.opentripplanner.routing.graphfinder.StopAtDistance;

public class QuayAtDistanceType {

  public static GraphQLObjectType createQD(GraphQLOutputType quayType, Relay relay) {
    return GraphQLObjectType.newObject()
            .name("QuayAtDistance")
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("id")
                    .type(new GraphQLNonNull(Scalars.GraphQLID))
                    .dataFetcher(environment -> relay.toGlobalId("QAD",
                        ((StopAtDistance) environment.getSource()).distance + ";" +
                            TransitIdMapper.mapEntityIDToApi(
                                ((StopAtDistance) environment.getSource()).stop
                            )
                    ))
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("quay")
                    .type(quayType)
                    .dataFetcher(environment -> ((StopAtDistance) environment.getSource()).stop)
                    .build())
            .field(GraphQLFieldDefinition.newFieldDefinition()
                    .name("distance")
                    .type(Scalars.GraphQLInt)
                    .dataFetcher(environment -> ((StopAtDistance) environment.getSource()).distance)
                    .build())
            .build();
  }
}
