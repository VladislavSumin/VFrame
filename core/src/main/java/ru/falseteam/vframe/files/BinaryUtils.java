package ru.falseteam.vframe.files;

import ru.falseteam.vframe.VFrameRuntimeException;

import java.io.*;

/**
 * Set of utilities from work with binary files.
 * Набор утилит для работы с бинарными файлами.
 *
 * @author Sumin Vladislav
 */
public class BinaryUtils {
    /**
     * Load binary file and cast him to type T.
     *
     * @param path path to file.
     * @param <T>  binary file cast to T
     * @return {T} if successfully read binary file of {null} if not.
     */
    @SuppressWarnings("unchecked")
    static <T> T loadFromBinaryFile(String path) {
        try {
            ObjectInputStream stream = new ObjectInputStream(new FileInputStream(path));
            T data = (T) stream.readObject();
            stream.close();
            return data;
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Save binary file to local storage
     *
     * @param data data to save
     * @param path path to save
     * @param <T>  type of data
     * @throws {@link RuntimeException} if can not save file.
     */
    @SuppressWarnings("JavaDoc")
    static <T> void saveToBinaryFile(T data, String path) {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(path));
            stream.writeObject(data);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            throw new VFrameRuntimeException(e);
        }
        //TODO а нужен ли здесь тип Т или можно object сохранять?
    }
}
