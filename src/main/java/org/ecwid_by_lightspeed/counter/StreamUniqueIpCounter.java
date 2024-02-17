package org.ecwid_by_lightspeed.counter;

import org.ecwid_by_lightspeed.helper.CustomBitSetHelper;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;

/**
 * Счетчик уникальных IP-адресов (реализация через Stream API)
 */
public class StreamUniqueIpCounter extends UniqueIpCounter {

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(String zipFileStr, String zipEntryStr) {
        var customBitSet = new CustomBitSetHelper();
        try (FileSystem zipFileSys = FileSystems.newFileSystem(Paths.get(zipFileStr));
             IntStream intStream = Files.lines(zipFileSys.getPath(zipEntryStr)).mapToInt(String::hashCode).parallel()) {
            return intStream.filter(hc -> !customBitSet.isSet(hc)).peek(customBitSet::set).count();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
