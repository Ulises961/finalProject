package utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class FileGenerator {
    private final File file;
    private String user;

    public FileGenerator(String name, String surname, String email) {
        this.file = new File("../Resources/" + name + "-" + surname + "-" + email + "-" + "+privKey.rsa");
    }

    public File getFile(BigInteger privKey) {

        try (FileOutputStream fos = new FileOutputStream(file);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            //convert string to byte array
            byte[] bytes = privKey.toByteArray();
            //write byte array to file
            bos.write(bytes);
            bos.close();
            fos.close();
            System.out.print("Data written to file successfully.");
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
