/**
 * Paul Felix
 * CEN-3024 - Software Development
 * 01/26/2024
 * BookLoader.java
 * This class is responsible for loading books into the library from a file. It provides functionality to prompt the user
 * for a file name and to read book information from the specified file. Each book's title and author are extracted from
 * the file and added to the library, facilitating the initial population of the library's book collection.
 */

package libraryms.libraryms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class BookLoader {

    /**
     * loadBooksFromFile
     * Loads books from a specified file into the library.
     * This private method reads the file line by line, parsing each book's title and author,
     * and then adds the book to the library.
     * The name of the file from which to load books.
     * The library instance to which the books will be added.
     */
    public static void loadBooksFromFile(String fileName, Library library) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String title = parts[0].trim();
                    String author = parts[1].trim();
                    String genre = parts[2].trim();
                    Book book = new Book(title, author, genre);
                    library.addBook(book);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
