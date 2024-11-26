package hu.ujfalusis.obm.conn.ws;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.eclipse.jetty.util.NanoTime;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import hu.ujfalusis.obm.dto.Message;
import hu.ujfalusis.obm.dto.OrderbookData;

public class ClientEndpoint implements Session.Listener {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientEndpoint.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Session session;
    private final String topic;
    private final Consumer<Message<OrderbookData>> consumer;

    private final ScheduledExecutorService pingpongExecutor;
    private final AtomicLong messageCounter = new AtomicLong(0);

    public ClientEndpoint(final String topic, final Consumer<Message<OrderbookData>> consumer) {
        this.topic = topic;
        this.consumer = consumer;
        this.pingpongExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onWebSocketOpen(Session session) {
        this.session = session;
        logger.info("ClientEndpoint session opened.");
        session.sendPing(ByteBuffer.allocate(8).putLong(NanoTime.now()).flip(), 
            Callback.from(
                () -> logger.info("ClientEndpoint socket open PING sended."), 
                (t) -> {logger.error("ClientEndpoint socket open PING failed!", t);}));
    // session.demand();
    }

    public void subscribe() {
        final String subscription = "{\"req_id\":\"" + UUID.randomUUID() + "\",\"op\":\"subscribe\",\"args\":[\"" + topic + "\"]}";
        logger.info("ClientEndpoint subscription message: {}.", subscription);
        session.sendText(subscription,
            Callback.from(
                () -> logger.info("ClientEndpoint subscription success."), 
                (t) -> {logger.error("ClientEndpoint subscription failed!", t);}));
        session.demand();
    }

    @Override
    public void onWebSocketText(String message) {
        try {
            messageCounter.incrementAndGet();
            final JsonNode tree = objectMapper.readTree(message);
            final String firstFieldName = tree.fieldNames().next();
            if (Objects.equals("topic", firstFieldName) && Objects.equals(tree.get(firstFieldName).asText(), this.topic)) {
                final JavaType pType = objectMapper.getTypeFactory().constructParametricType(Message.class, OrderbookData.class);
                final Message<OrderbookData> messageObj = objectMapper.treeToValue(tree, pType);

                consumer.accept(messageObj);

            } else if (Objects.equals("success", firstFieldName)) {
                logger.info("Subscription reply message received: {}.", message);
            } else {
                logger.warn("ClientEndpoint unknown message received: {}.", message);
            }
            session.demand();
        } catch (JsonProcessingException e) {
            logger.error("Error while running message receiver thread!", e);
        }
    }

    @Override()
    public void onWebSocketPong(ByteBuffer payload) {
        final long start = payload.getLong();
        final double roundTrip = NanoTime.since(start) / 1e9;
        logger.info("ClientEndpoint new PONG received, roundTrip: {} seconds, message counter: {}", roundTrip, messageCounter.get());

        Runnable task = () -> {
            session.sendPing(ByteBuffer.allocate(8).putLong(NanoTime.now()).flip(), 
            Callback.from(
                () -> logger.info("ClientEndpoint PING successfully sended."), 
                (t) -> {logger.error("ClientEndpoint PING failed!", t);}));
        };
        pingpongExecutor.schedule(task, 30, TimeUnit.SECONDS);

        session.demand();
    }

    public void stop() {
        pingpongExecutor.shutdown();
        session.disconnect();
    }
}
