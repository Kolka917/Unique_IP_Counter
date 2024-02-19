package org.ecwid_by_lightspeed.counter;

import org.ecwid_by_lightspeed.helper.CustomBitSetHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Счетчик уникальных IP-адресов (реализация через BufferedReader)
 */
public class BufferReadUniqueIpCounter extends UniqueIpCounter {

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(String zipFileStr, String zipEntryStr) {
        try {
            var customBitSet = new CustomBitSetHelper();
            int numThreads = Runtime.getRuntime().availableProcessors();
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            Consumer<String> processingLineFunction = line -> {
                try {
                    customBitSet.set(InetAddress.getByName(line).hashCode());
                } catch (UnknownHostException e) {
                    throw new RuntimeException("Invalid IPv4 address");
                }
            };

            for (int i = 0; i < numThreads; i++) {
                Runnable task = createReadTask(zipFileStr, zipEntryStr, numThreads, i, processingLineFunction);
                executorService.submit(task);
            }

            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);

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

                long fileSize = Files.size(zipFileSys.getPath(zipEntryStr));
                long portionSize = fileSize / numThreads;
                long start = threadIndex * portionSize;
                long end = (threadIndex == numThreads - 1) ? fileSize : (threadIndex + 1) * portionSize;

                long bytesRead = start;
                reader.skip(start);

                int nextChar;
                while (start != 0 && bytesRead < end && (nextChar = reader.read()) != -1 && nextChar != '\n') {
                    bytesRead++;
                }

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
