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

public class SessionCreationException extends Exception {
	private static final long serialVersionUID = 5398002228641189789L;

	public SessionCreationException() {
    }

    public SessionCreationException(String message) {
        super(message);
    }

}