package hu.ujfalusis.obm.conn.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;

import de.siegmar.fastcsv.writer.CsvWriter;
import hu.ujfalusis.obm.OBMException;
import hu.ujfalusis.obm.dto.CSVEntry;


public class Filewriter {
    private final CsvWriter writer;
    private final DateTimeFormatter dtf; 

    public Filewriter() {
        final Path outp = Path.of("output.csv");
        final Path archiveDir = Path.of("archive");
        this.dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        try {
            if (Files.exists(outp)) {
                if (!Files.exists(archiveDir)) {
                    Files.createDirectory(archiveDir);
                }
                Files.move(outp, archiveDir.resolveSibling("archive/output-" + System.currentTimeMillis() + ".csv"));
            }
        } catch (IOException e) {
            throw new OBMException("Error occured while archiving output.csv!", e);
        }

        try {
            this.writer = CsvWriter.builder().build(outp, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            this.writer.writeRecord("server_time", "best_bid_price", "best_ask_price", "avg_buy_price", "avg_sell_price");
        } catch (IOException e) {
            throw new OBMException("Error occured while making output.csv!", e);
        }
    }

    public void write(CSVEntry entry) {
        writer.writeRecord(
            dtf.format(entry.getServerTime()), 
            String.valueOf(entry.getBestBidPrice()),
            String.valueOf(entry.getBesAskPrice()),
            String.valueOf(entry.getAvgBuyPrice()),
            String.valueOf(entry.getAvgSellPrice()));
        try {
            writer.flush();
        } catch (IOException e) {
            throw new OBMException("Error occured while writing CSV file!", e);
        }
    }
}
