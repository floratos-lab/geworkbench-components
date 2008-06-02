package org.geworkbench.components.caarray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.caarray.arraydata.CaArray2Component;

abstract public class BaseApiClient {
    //protected static final String SERVER_NAME = "array.nci.nih.gov ";
    protected static final String SERVER_NAME = "array-stage.nci.nih.gov ";
    protected static final int JNDI_PORT = 8080;
    protected static final int GRID_SERVICE_PORT = 8080;
    public static Log log = LogFactory.getLog(CaArray2Component.class);

}
