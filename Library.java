/**
 * Paul Felix
 * CEN-3024 - Software Development
 * 01/26/2024
 * Library.java
 * This class serves as an assistance class to the main class that supports the application.
 */

package libraryms.libraryms;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Library {
    public static ArrayList<Book> books = new ArrayList<>();

    public static List<Book> getAllBooks() {
        return Collections.unmodifiableList(books);
    }


    /**
     * addBook
     * Adds a book to the library's collection.
     * Method receive an object type book.
     */
    public void addBook(Book book) {
        if (book != null) {
            books.add(book);
        } else {
            System.out.println("Cannot add a null book to the library.");
        }
    }

}








