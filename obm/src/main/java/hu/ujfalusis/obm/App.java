package hu.ujfalusis.obm;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.ujfalusis.obm.bl.Manager;
import hu.ujfalusis.obm.conn.fs.Filewriter;
import hu.ujfalusis.obm.conn.ws.WSocketClient;
import hu.ujfalusis.obm.dto.CSVEntry;
import hu.ujfalusis.obm.dto.Message;
import hu.ujfalusis.obm.dto.OrderbookData;
import hu.ujfalusis.obm.dto.OrderbookSide;
import hu.ujfalusis.obm.dto.RetrieveResult;

/**
 * Hello world!
 *
 */
public class App {
    
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static final String STREAM_MAINNET_DOMAIN = "wss://stream.bybit.com";
    public static final String STREAM_TESTNET_DOMAIN = "wss://stream-testnet.bybit.com";
    public static final String V5_PUBLIC_LINEAR = "/v5/public/linear";
    public static final String TOPIC = "orderbook.50.BTCUSDT";

    public static void main(String[] args) throws SecurityException, IOException {

        final ExecutorService processExecutor = Executors.newSingleThreadExecutor(); // Executors.newCachedThreadPool(); use for parallel execution
        final ExecutorService fileExecutor = Executors.newSingleThreadExecutor();
        final BlockingQueue<CSVEntry> queue = new LinkedBlockingQueue<>();
        
        final Filewriter fileWriter = new Filewriter();
        final Manager manager = new Manager();
        
        Consumer<Message<OrderbookData>> consumer = (message) -> {
            processExecutor.submit(() -> {
                try {
                    final OrderbookData data = message.getData();
                    manager.process(data, message.getType());
                    final RetrieveResult ask = manager.retrieve(250_000_000, OrderbookSide.ASKS, data.getSeq());
                    final RetrieveResult bid = manager.retrieve(250_000_000, OrderbookSide.BIDS, data.getSeq());
                    final LocalDateTime serverTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getTs()), ZoneId.systemDefault()); // with local timezone
                    final CSVEntry csvEntry = new CSVEntry(serverTime, bid.getBest(), ask.getBest(), ask.getAvaragePrice(), bid.getAvaragePrice());
                    queue.offer(csvEntry);
                } catch (Exception e) {
                    logger.error("Error while running process thread!", e);
                }
            });
        };

        fileExecutor.submit(() -> {
            try {
                while (true) {
                    final CSVEntry csvEntry = queue.take();
                    fileWriter.write(csvEntry);
                }
            } catch (Exception e) {
                logger.error("Error while running file writer thread!", e);
            }
        });

        final WSocketClient client = new WSocketClient(STREAM_MAINNET_DOMAIN + V5_PUBLIC_LINEAR, TOPIC, consumer);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("SHUTDOWN started.");
            client.disconnect();
            LifeCycle.stop(client);
            processExecutor.shutdown();
            fileExecutor.shutdown();
            logger.info("SHUTDOWN successfully executed.");
            logger.info("Bye");
        }));

        client.connectToServer();
    }
}
