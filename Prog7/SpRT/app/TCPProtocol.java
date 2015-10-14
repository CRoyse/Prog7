package SpRT.app;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.logging.Logger;

import SPA.app.SPAServer;

/**
 * Interface between our SpRTServerSIO and specific handling protocol
 * @author Corey Royse
 * Assignment: Program 6
 */
public interface TCPProtocol {
	void handleAccept(SelectionKey key) throws IOException;
	void handleRead(SelectionKey key, Logger l, SPAServer spaServ) throws IOException;
	void handleWrite(SelectionKey key) throws IOException;
}
