package org.ecwid_by_lightspeed.counter;

import org.ecwid_by_lightspeed.helper.CustomBitSetHelper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

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
        ToIntFunction<String> mapToHashFunc = line -> {
            try {
                return InetAddress.getByName(line).hashCode();
            } catch (UnknownHostException e) {
                throw new RuntimeException("Invalid IPv4 address");
            }
        };
        try (FileSystem zipFileSys = FileSystems.newFileSystem(Paths.get(zipFileStr));
             Stream<String> stream = Files.lines(zipFileSys.getPath(zipEntryStr)).parallel()) {
            return stream.mapToInt(mapToHashFunc).filter(hc -> !customBitSet.isSet(hc)).peek(customBitSet::set).count();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
