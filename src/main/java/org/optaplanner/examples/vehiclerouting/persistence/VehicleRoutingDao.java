package org.optaplanner.examples.vehiclerouting.persistence;

import org.optaplanner.examples.common.persistence.XStreamSolutionDao;
import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;

public class VehicleRoutingDao extends XStreamSolutionDao<VehicleRoutingSolution> {

    public VehicleRoutingDao() {
        super("vehiclerouting", VehicleRoutingSolution.class);
    }

}
