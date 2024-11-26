package hu.ujfalusis.obm.conn.ws;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.ujfalusis.obm.OBMException;
import hu.ujfalusis.obm.dto.Message;
import hu.ujfalusis.obm.dto.OrderbookData;


public class WSocketClient {

    private static final Logger logger = LoggerFactory.getLogger(WSocketClient.class);

    private final WebSocketClient client;
    private final ClientEndpoint clientEndpoint;
    private final URI serverURI;

    public WSocketClient(final String uri, final String topic, final Consumer<Message<OrderbookData>> consumer) {
        this.clientEndpoint = new ClientEndpoint(topic, consumer);
        client = new WebSocketClient();
        serverURI = URI.create(uri);

    }

    public void connectToServer() {
        try {
            client.start();
            CompletableFuture<Session> clientSessionPromise = client.connect(clientEndpoint, serverURI);
            clientSessionPromise.thenRun(() -> {
                logger.info("WebSocket client connected to server: {}.", serverURI);
                clientEndpoint.subscribe();
            });
        } catch (Exception e) {
            throw new OBMException("Error occured while connecting to WebSocket server!", e);
        }
    }

    public void disconnect() {
        try {
            clientEndpoint.stop();
            client.stop();
        } catch (Exception e) {
            throw new OBMException("Error occured while stopping WebSocket client!", e);
        }
    }
}
