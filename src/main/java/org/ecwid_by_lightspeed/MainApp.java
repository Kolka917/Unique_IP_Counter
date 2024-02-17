package org.ecwid_by_lightspeed;

import org.ecwid_by_lightspeed.counter.BufferReadUniqueIpCounter;
import org.ecwid_by_lightspeed.counter.StreamUniqueIpCounter;
import org.ecwid_by_lightspeed.counter.UniqueIpCounter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Счетчик уникальных IP-адресов в указанном файле
 */
public class MainApp {
    private static final String ZIP_FILE_PATH = "<Путь до zip-файла>";
    private static final String ZIP_ENTRY_NAME = "<Название файла>";


    public static void main(String[] args) {
        runCount(new StreamUniqueIpCounter(), "StreamResult");
        runCount(new BufferReadUniqueIpCounter(), "BRResult");
    }

    /**
     * Запуск подсчета
     *
     * @param uniqueIpCounter Счетчик уникальных IP адресов
     * @param fileName        Название файла
     */
    private static void runCount(UniqueIpCounter uniqueIpCounter, String fileName) {
        var startMs = System.currentTimeMillis();
        var uniqueIpCountStream = uniqueIpCounter.count(ZIP_FILE_PATH, ZIP_ENTRY_NAME);
        var endMs = System.currentTimeMillis();
        writeResult(fileName, uniqueIpCountStream, startMs, endMs);
    }

    /**
     * Запись результата в файл
     *
     * @param fileName      Название файла
     * @param uniqueIpCount Число уникальных IP адресов
     * @param startMs       Временная метка начала процесса обработки
     * @param endMs         Временная метка конца процесса обработки
     */
    private static void writeResult(String fileName, long uniqueIpCount, long startMs, long endMs) {
        String filePath = String.format("src/main/resources/%s.txt", fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(String.format("Уникальных IP адресов: %s, время обработки: %s сек", uniqueIpCount, (endMs - startMs) / 1000));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
