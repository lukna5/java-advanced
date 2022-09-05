package info.kgeorgiy.ja.kononov.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Walk {

    private static final String NULL_HASH = "0000000000000000000000000000000000000000 ";
    private static final int BUFFER_SIZE = 1024;
    // :NOTE: formatting

    private static Path checkPath(String path, String file) {
        try {
            return Paths.get(path);
        } catch (InvalidPathException e) {
            System.err.println("Can't understand path of " + file + " file: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {

        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.err.println("Please write 2 arguments: \"input output\"");
            return;
        }

        final Path input = checkPath(args[0], "input");
        final Path output = checkPath(args[1], "output");

        if (input != null && output != null) {
            if (output.getParent() != null) {
                try {
                    Files.createDirectories(output.getParent());
                } catch (IOException e) {
                    System.err.println("Can't create directory for output file " + e.getMessage());
                    return;
                }
            }

            try (BufferedReader inReader = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
                try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
                    String next;
                    while ((next = inReader.readLine()) != null) {
                        // :NOTE: mb is a bad name, 1024 move to a const
                        byte[] readiedBytes = new byte[BUFFER_SIZE];
                        File nextInFile = new File(next);

                        try (FileInputStream reader = new FileInputStream(nextInFile)) {
                            MessageDigest digest = MessageDigest.getInstance("SHA-1");
                            int readied;
                            while ((readied = reader.read(readiedBytes)) > 0) {
                                digest.update(readiedBytes, 0, readied);
                            }
                            byte[] mdBytes = digest.digest();

                            StringBuilder sb = new StringBuilder();
                            for (byte mdByte : mdBytes) {
                                sb.append(String.format("%02x", mdByte));
                            }
                            // :NOTE: \n

                            writer.write(sb + " " + next + "\n");

                        } catch (NoSuchAlgorithmException e) {
                            System.err.println("Dont known this encoding algorithm " + e.getMessage());
                        } catch (IOException e) {
                            // :NOTE: 0000 move to a const value, \n
                            writer.write(NULL_HASH + next + "\n");
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.err.println("FileNotFoundException with output file " + e.getMessage());
                } catch (IOException e) {
                    System.err.println("Something wrong with output file " + e.getMessage());
                }
            } catch (IOException e) {
                System.err.println("Something wrong with input file " + e.getMessage());
            }
        }
    }
}