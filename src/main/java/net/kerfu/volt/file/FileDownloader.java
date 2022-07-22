package net.kerfu.volt.file;

import lombok.Getter;
import net.kerfu.volt.loader.Loader;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@Getter
public class FileDownloader {

    private final URL url;
    private File root;

    public FileDownloader(String link) {
        try {
            this.url = new URL(link);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        this.root = new File("/etc/");
    }

    public FileDownloader(String link, File root) {
        this(link);
        this.root = root;
    }

    public File download() {
        InputStream inputStream = null;
        File file = null;
        FileOutputStream fileOutputStream = null;
        try {
            file = File.createTempFile(Loader.getPrefix(), ".jar");

            fileOutputStream = new FileOutputStream(file);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla 5.0 (Windows; U; "
                    + "Windows NT 5.1; en-US; rv:1.8.0.11) ");
            inputStream = urlConnection.getInputStream();

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException exception) {
                    exception.printStackTrace();;
                }
            }
        }
        return file;
    }
}
