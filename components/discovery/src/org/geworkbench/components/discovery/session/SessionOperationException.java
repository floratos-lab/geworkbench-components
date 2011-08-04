package org.geworkbench.components.discovery.session;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version $Id$
 */

public class SessionOperationException extends Exception {
	private static final long serialVersionUID = -4038098262013841518L;

	public SessionOperationException() {
    }

    public SessionOperationException(String message) {
        super(message);
    }
}