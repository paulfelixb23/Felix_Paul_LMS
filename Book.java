/**
 * Paul Felix
 * CEN-3024 - Software Development
 * 01/26/2024
 * Book.java
 * This class represents a book in the library management system. It includes properties for the book's ID, title, and author.
 * The book ID is auto-generated to ensure uniqueness. This class is fundamental in managing books within the library,
 * allowing for operations such as checking out and returning books.
 */

package libraryms.libraryms;
import java.util.concurrent.atomic.AtomicInteger;

class Book {
    private static final AtomicInteger idGenerator = new AtomicInteger(1000000);
    private final int bookId;
    private String title;
    private String author;
    private String genre;
    private boolean isCheckedOut;


    public Book(String title, String author, String genre) {
        this.bookId = idGenerator.getAndIncrement();
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isCheckedOut = false;
    }


    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        return "Book ID: " + bookId + ", Title: " + title + ", Author: " + author + (isCheckedOut ? ", Status: Checked out" : ", Status: Available");
    }
}