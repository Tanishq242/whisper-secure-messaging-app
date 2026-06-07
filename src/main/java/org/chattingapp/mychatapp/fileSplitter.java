package org.chattingapp.mychatapp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class fileSplitter {
    public static void split() {
        try {
            // Read file into byte array

            byte[] fileBytes = Files.readAllBytes(Paths.get("qara.mp3"));

            // Find the midpoint
            int mid = fileBytes.length / 2;

            // First half
            byte[] part1 = new byte[mid];
            System.arraycopy(fileBytes, 0, part1, 0, mid);

            System.out.println("Part 1");
            // for (byte b : part1) {
            //     System.out.println(b);
            // }

            // Second half
            byte[] part2 = new byte[fileBytes.length - mid];
            System.arraycopy(fileBytes, mid, part2, 0, fileBytes.length - mid);

            System.out.println("Part 2");
            // for (byte b : part2) {
            //     System.out.println(b);
            // }

            // Save the two parts
            FileOutputStream fos1 = new FileOutputStream("firstHalfM1.mp3");
            FileOutputStream fos2 = new FileOutputStream("secondHalfM2.mp3");
            fos1.write(part1);
            fos2.write(part2);

            fos1.close();
            fos2.close();

            System.out.println("File split into two parts successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void merge() {
        try {
            // Read file into byte array
            byte[] part1 = Files.readAllBytes(Paths.get("firstHalf.bin"));
            byte[] part2 = Files.readAllBytes(Paths.get("secondHalf.bin"));

            byte[] mergeFile = new byte[part1.length + part2.length];

            // Copy first part
            System.arraycopy(part1, 0, mergeFile, 0, part1.length);

            // Copy second part
            System.arraycopy(part2, 0, mergeFile, part1.length, part2.length);

            FileOutputStream fos = new FileOutputStream("example_merged.jpg");
            fos.write(mergeFile);
            fos.close();
            System.out.println("Files merged successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
