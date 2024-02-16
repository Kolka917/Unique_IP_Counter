package org.ecwid_by_lightspeed.counter;

import org.ecwid_by_lightspeed.helper.CustomBitSetHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Счетчик уникальных IP-адресов в указанном файле
 */
public class UniqueIpCounter {

    private static final String IP_PATTERN = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    /**
     * Реализация через Stream Api
     *
     * @param zipFileStr  Путь до zip архива
     * @param zipEntryStr Название файла внутри zip архива
     * @return Число уникальных IP адресов
     */
    public long countByStreamApi(String zipFileStr, String zipEntryStr) {
        var customBitSet = new CustomBitSetHelper();
        try (FileSystem zipFileSys = FileSystems.newFileSystem(Paths.get(zipFileStr));
             IntStream intStream = Files.lines(zipFileSys.getPath(zipEntryStr)).mapToInt(String::hashCode).parallel()) {
            return intStream.filter(hc -> !customBitSet.isSet(hc)).peek(customBitSet::set).count();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Реализация через Buffered Reader
     *
     * @param zipFileStr   Путь до zip архива
     * @param zipEntryStr  Название файла внутри zip архива
     * @param numThreads   Число потоков, на которые разобьется обработка
     * @param isUsePattern Признак необходимости применять регулярное выражение на соответствие IPv4 адресу
     * @return Число уникальных IP адресов
     */
    public long countByBufferedReader(String zipFileStr, String zipEntryStr, int numThreads, boolean isUsePattern) {
        var customBitSet = new CustomBitSetHelper();
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        Consumer<String> processingLineFunction = (line) -> {
                if (!isUsePattern || line.matches(IP_PATTERN)) {
                    customBitSet.set(line.hashCode());
                }
        };

        for (int i = 0; i < numThreads; i++) {
            Runnable task = createReadTask(zipFileStr, zipEntryStr, numThreads, i, processingLineFunction);
            executorService.submit(task);
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
            return customBitSet.countSetBits();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Runnable createReadTask(String zipFileStr, String zipEntryStr, int numThreads, int threadIndex, Consumer<String> processingLineFunction) {
        return () -> {
            try (FileSystem zipFileSys = FileSystems.newFileSystem(Paths.get(zipFileStr));
                 InputStream inputStream = Files.newInputStream(zipFileSys.getPath(zipEntryStr));
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                long fileSize = inputStream.available();
                long portionSize = fileSize / numThreads;
                long start = threadIndex * portionSize;
                long end = (threadIndex == numThreads - 1) ? fileSize : (threadIndex + 1) * portionSize;

                long bytesRead = start;
                reader.skip(start);
                String line;
                while (bytesRead < end && (line = reader.readLine()) != null) {
                    bytesRead += line.getBytes().length + 1;
                    processingLineFunction.accept(line);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
