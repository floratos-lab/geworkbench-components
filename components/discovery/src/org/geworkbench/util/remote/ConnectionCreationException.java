package org.geworkbench.util.remote;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version $Id$
 */

public class ConnectionCreationException extends Exception {
	private static final long serialVersionUID = 3851927404373223292L;

	public ConnectionCreationException() {
    }

    public ConnectionCreationException(String message) {
        super(message);
    }
}