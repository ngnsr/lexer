package org.example;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class CustomFileReader implements Readable {
    private final Path path;

    CustomFileReader(Path path){
        this.path = path;
    }

    @Override
    public String read() throws IOException {
        return Files.readString(path);
    }
}
