package de.cyzetlc.hsbi.game.network.packets;

import java.io.*;

public class SerializationUtils {
    /**
     * The function serializes an object into a byte array.
     *
     * @param object The "object" parameter is the object that you want to serialize into a byte array.
     * @return The method is returning a byte array.
     */
    public static byte[] serialize(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out;
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * The function deserializes a byte array into an object of the specified class type.
     *
     * @param bytes The "bytes" parameter is a byte array that contains the serialized object data.
     * @param clazz The "clazz" parameter is a Class object that represents the type of the object that you want to
     * deserialize from the byte array. It is used to cast the deserialized object to the appropriate type before returning
     * it.
     * @return The method is returning an object of type T, which is determined by the Class parameter clazz.
     */
    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try (ObjectInput in = new ObjectInputStream(bis)) {
            return clazz.cast(in.readObject());
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
