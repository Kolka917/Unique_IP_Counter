package org.ecwid_by_lightspeed;

import org.ecwid_by_lightspeed.counter.UniqueIpCounter;

/**
 * Счетчик уникальных IP-адресов в указанном файле
 */
public class MainApp {
    private static final String ZIP_FILE_PATH = "<Путь до zip-файла>";
    private static final String ZIP_ENTRY_NAME = "<Название файла>";


    public static void main(String[] args) {
        readByStreamApi();
        readByBufferedReader();
    }

    private static void readByStreamApi() {
        var uniqueIpCounter = new UniqueIpCounter();
        var startMsStream = System.currentTimeMillis();
        var uniqueIpCountStream = uniqueIpCounter.countByStreamApi(ZIP_FILE_PATH, ZIP_ENTRY_NAME);
        var endMsStream = System.currentTimeMillis();
        System.out.printf("Stream API. Уникальных IP адресов: %s, время обработки: %s сек%n",
                uniqueIpCountStream,
                (endMsStream - startMsStream) / 1000);
    }

    private static void readByBufferedReader() {
        var uniqueIpCounter = new UniqueIpCounter();
        var startMsBR = System.currentTimeMillis();
        int numThreads = 4;
        var uniqueIpCountBR = uniqueIpCounter.countByBufferedReader(ZIP_FILE_PATH, ZIP_ENTRY_NAME, numThreads, true);
        var endMsBR = System.currentTimeMillis();
        System.out.printf("BufferedReader. Уникальных IP адресов: %s, время обработки: %s сек%n",
                uniqueIpCountBR,
                (endMsBR - startMsBR) / 1000);
    }
}
