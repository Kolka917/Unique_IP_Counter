package org.ecwid_by_lightspeed.counter;

/**
 * Абстрактный класс - счетчик уникальных IP-адресов
 */
public abstract class UniqueIpCounter {

    /**
     * Подсчет уникальных IP-адресов
     *
     * @param zipFileStr  Путь до zip архива
     * @param zipEntryStr Название файла внутри zip архива
     * @return Число уникальных IP адресов
     */
    public abstract long count(String zipFileStr, String zipEntryStr);
}
