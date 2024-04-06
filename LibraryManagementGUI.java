/**
 * Paul Felix
 * CEN-3024 - Software Development
 * 01/26/2024
 * LibraryManagementGUI.java
 * This the main class for the application that manages a collection of books in the library. Handles the creation of
 * the actual user interface. It supports adding books to the library, removing books by ID checking books in and out,
 * and displaying the current inventory of books. The class plays a crucial role in the library management system,
 * ensuring that operations related to book handling are executed efficiently.
 */

package libraryms.libraryms;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class LibraryManagementGUI extends Application {
    private final Library library = new Library();
    private final TextArea textArea = new TextArea();
    private Window primaryStage;


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Library Management System");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        GridPane buttonGrid = new GridPane();
        buttonGrid.setVgap(10);
        buttonGrid.setHgap(10);
        buttonGrid.setPadding(new Insets(15, 0, 15, 0));

        Button btnAddBook = new Button("Add Book");
        Button btnLoadBooks = new Button("Load Books from File");
        Button btnRemoveBookByBarcode = new Button("Remove Book by Barcode");
        Button btnRemoveBookByTitle = new Button("Display Book List");
        Button btnCheckOutBook = new Button("Check Out Book");
        Button btnCheckInBook = new Button("Check In Book");
        Button btnExit = new Button("Exit");


        buttonGrid.add(btnAddBook, 0, 0);
        buttonGrid.add(btnLoadBooks, 1, 0);
        buttonGrid.add(btnRemoveBookByBarcode, 0, 1);
        buttonGrid.add(btnRemoveBookByTitle, 1, 1);
        buttonGrid.add(btnCheckOutBook, 0, 2);
        buttonGrid.add(btnCheckInBook, 1, 2);
        buttonGrid.add(btnExit, 0, 3, 2, 1); // Span 2 columns


        btnAddBook.setMinWidth(280);
        btnLoadBooks.setMinWidth(280);
        btnRemoveBookByBarcode.setMinWidth(280);
        btnRemoveBookByTitle.setMinWidth(280);
        btnCheckOutBook.setMinWidth(280);
        btnCheckInBook.setMinWidth(280);
        btnExit.setMinWidth(570);


        btnAddBook.setOnAction(e -> addBook());
        btnLoadBooks.setOnAction(e -> loadBooks());
        btnRemoveBookByTitle.setOnAction(e -> displayBooks());
        btnRemoveBookByBarcode.setOnAction(e -> removeBookByBarcode());
        btnCheckInBook.setOnAction(e -> checkInBook());
        btnCheckOutBook.setOnAction(e -> checkOutBook());
        btnExit.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(buttonGrid, textArea);

        Scene scene = new Scene(root, 700, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * addBook
     * Prompts the user to input Title, author, and genre. Uses connection to the DB already implemented and sends
     * command to the DB to insert new value into the table, and it gets updated on the DB, if action could not be
     * completed show alert to the user.
     */
    private void addBook() {

        TextInputDialog titleDialog = new TextInputDialog();
        titleDialog.setTitle("Add New Book");
        titleDialog.setHeaderText("Add Book");
        titleDialog.setContentText("Please enter the book title:");


        TextInputDialog authorDialog = new TextInputDialog();
        authorDialog.setTitle("Add New Book");
        authorDialog.setHeaderText("Add Book");
        authorDialog.setContentText("Please enter the author name:");


        TextInputDialog genreDialog = new TextInputDialog();
        genreDialog.setTitle("Add New Book");
        genreDialog.setHeaderText("Add Book");
        genreDialog.setContentText("Please enter the book genre:");

        Optional<String> titleResult = titleDialog.showAndWait();
        titleResult.ifPresent(title -> {
            Optional<String> authorResult = authorDialog.showAndWait();
            authorResult.ifPresent(author -> {
                Optional<String> genreResult = genreDialog.showAndWait();
                genreResult.ifPresent(genre -> {
                    String sql = "INSERT INTO books (title, author, genre) VALUES (?, ?, ?)";

                    try (Connection conn = DatabaseConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setString(1, title);
                        pstmt.setString(2, author);
                        pstmt.setString(3, genre);
                        int affectedRows = pstmt.executeUpdate();

                        if (affectedRows > 0) {
                            showAlert("Success", "New book added successfully.", Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Error", "Failed to add the book.", Alert.AlertType.ERROR);
                        }
                        displayBooks(); // Refresh the book list
                    } catch (SQLException e) {
                        showAlert("Database Error", "Failed to add the book to the database: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            });
        });
    }

    /**
     * loadBooks
     * Initiates the process of fetching the current list of books from the database. It establishes a connection
     * using the existing database connection mechanism within the application. Upon successful retrieval, it updates
     * the graphical user interface to display the latest collection of books, allowing users to view the entire inventory.
     * If the connection to the database fails or the data cannot be fetched for any reason, an error alert is presented
     * to the user. This method ensures that the library's inventory is accurately reflected within the application.
     */
    private void loadBooks() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Books from File");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            BookLoader.loadBooksFromFile(selectedFile.getAbsolutePath(), library);

            List<Book> books = Library.getAllBooks();

            String sql = "INSERT INTO books (title, author, genre) VALUES (?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                for (Book book : books) {
                    pstmt.setString(1, book.getTitle());
                    pstmt.setString(2, book.getAuthor());
                    pstmt.setString(3, book.getGenre());
                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                showAlert("Success", "Books loaded and added to database successfully.", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to add loaded books to the database: " + e.getMessage(), Alert.AlertType.ERROR);
            }

            displayBooks();
        } else {
            System.out.println("File selection cancelled.");
        }
    }

    /**
     * displayBooks
     * This method is responsible for rendering the list of books currently available in the library onto the graphical user interface (GUI).
     * It retrieves the updated book collection from the application's internal data structures or database and dynamically updates the display area
     * designated for book listings. Each book's information, including title, author, and genre, is formatted and presented in a user-friendly manner.
     * In case there are no books to display, it may show a message indicating the library is currently empty or adjust the display accordingly.
     * This method plays a critical role in ensuring that the users have real-time access to the library's inventory, enhancing user interaction and engagement.
     */
    private void displayBooks() {
        String query = "SELECT * FROM books";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            StringBuilder booksList = new StringBuilder("Books in the library:\n\n");
            while (rs.next()) {
                booksList.append("ID: ").append(rs.getInt("book_id"))
                        .append(", Title: ").append(rs.getString("title"))
                        .append(", Author: ").append(rs.getString("author"))
                        .append(", Status: ").append(rs.getString("status"))
                        .append(", Due Date: ").append(rs.getString("due_date") == null ? "N/A" : rs.getString("due_date"))
                        .append(", Genre: ").append(rs.getString("genre"))
                        .append("\n");
            }
            textArea.setText(booksList.toString());
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to fetch books from the database.", Alert.AlertType.ERROR);
        }
    }

    /**
     * removeBookByBarcode
     * Enables the deletion of a specific book from the library's database using its unique barcode as a reference.
     * The method prompts the user for the barcode of the book to be removed, then initiates a search within the database
     * to locate this book. If the book is found, it is removed from the database, and a confirmation alert is displayed to
     * the user, indicating the successful deletion. In case the book cannot be found or the removal process encounters an
     * error, an error alert is shown to the user to indicate the failure of the operation. This functionality is crucial for
     * maintaining an accurate and current inventory in the library management system, allowing for the removal of lost,
     * damaged, or otherwise unavailable books.
     */
    private void removeBookByBarcode() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove Book");
        dialog.setHeaderText("Remove Book by ID");
        dialog.setContentText("Please enter the book ID:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(bookIdStr -> {
            try {
                int bookId = Integer.parseInt(bookIdStr);
                String sql = "DELETE FROM books WHERE book_id = ?";

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, bookId);
                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows > 0) {
                        showAlert("Success", "Book with ID " + bookId + " removed successfully.", Alert.AlertType.INFORMATION);
                    } else {
                        showAlert("Error", "No book found with ID " + bookId + ".", Alert.AlertType.ERROR);
                    }
                    displayBooks(); // Refresh the book list
                } catch (SQLException e) {
                    showAlert("Database Error", "Failed to remove the book from the database: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Book ID must be a number.", Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * checkInBook
     * Handles the operation of checking a book back into the library's inventory. This method is typically invoked
     * when a user returns a book they had borrowed. It prompts the user for the book's identification details, which
     * could be a barcode that is unique identifier used by the library system. Upon receiving the input, it
     * verifies the book's current status to ensure it is indeed out on loan and then updates the database to reflect
     * that the book is now available for borrowing again. A confirmation message is displayed to the user to indicate
     * the successful check-in of the book. If the book cannot be found or is already checked in, an error message is
     * shown. This method is crucial for maintaining the accuracy of the library's inventory and ensuring books are
     * available to be borrowed by other users.
     */
    private void checkInBook() {
        int bookId = askForBookId("Check In Book", "Enter the book ID to check in:");
        if (bookId == -1) return; // User canceled or entered invalid data

        String sql = "UPDATE books SET status = 'checked in', due_date = NULL WHERE book_id = ?";
        executeBookStatusUpdate(sql, bookId, "checked in");
    }

    /**
     * checkOutBook
     * Manages the procedure of borrowing a book from the library, marking it as checked out in the library's inventory system.
     * This method prompts the user for the necessary identification details of the book they wish to borrow, such as its barcode.
     * Upon input, it verifies the availability of the book for checkout. If the book is available, the system updates
     * its status to checked out, associates the book with the user's account, and calculates the due date based on the library's
     * lending policies. A confirmation message is then displayed to the user, indicating the successful checkout and reminding them
     * of the return date. In cases where the book is not available for checkout—either because it's already borrowed, reserved,
     * or does not exist—an appropriate error message is shown. This method is essential for facilitating the library's lending services,
     * ensuring users can borrow books while maintaining an accurate and updated inventory.
     */
    private void checkOutBook() {
        int bookId = askForBookId("Check Out Book", "Enter the book ID to check out:");
        if (bookId == -1) return;

        String sql = "UPDATE books SET status = 'checked out', due_date = ? WHERE book_id = ? AND status = 'checked in'";
        executeBookStatusUpdate(sql, bookId, "checked out");
    }

    /**
     * askForBookId
     * Initiates a dialogue with the user to input the identification number (ID) of a book. This method is typically
     * used in scenarios where a specific book's ID is required to perform operations such as checking out, checking in,
     * or removing a book from the library's inventory. It presents a user interface element, such as a dialog box, where
     * the user can enter the book's ID. Upon submission, the entered ID is validated for its format and existence within
     * the library's database. If the ID is valid, it is returned for further processing in the operation. In case of invalid
     * input such as non-existent book ID, incorrect format, the user is alerted and prompted to re-enter the information
     * correctly. This method ensures that operations involving specific books are carried out efficiently and accurately.
     */
    private int askForBookId(String title, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(content);
        Optional<String> result = dialog.showAndWait();
        try {
            return result.map(Integer::parseInt).orElse(-1);
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid book ID.", Alert.AlertType.ERROR);
            return -1;
        }
    }

    /**
     * executeBookStatusUpdate
     * This method is responsible for updating the status of a book within the library's management system database. It is
     * typically called after operations that change a book's availability status, such as checking out, checking in, or
     * reserving a book. The method takes as input the book's identification number and the new status to be applied. It first
     * verifies that the book exists within the database and that the requested status change is valid based on the book's
     * current state. For example, a book cannot be checked out if it is already checked out or reserved. If the status update
     * is valid, the method proceeds to update the book's record in the database with the new status. Upon successful update,
     * it may return a confirmation to the calling process or update the user interface to reflect the change. In case of
     * failure, due to reasons such as invalid book ID or inappropriate status transition, an error is generated, and the
     * operation is aborted. This method plays a crucial role in maintaining the integrity and accuracy of the library's
     * inventory system.
     */
    private void executeBookStatusUpdate(String sql, int bookId, String action) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (action.equals("checked out")) {
                pstmt.setString(1, LocalDate.now().plusWeeks(2).toString());
                pstmt.setInt(2, bookId);
            } else {
                pstmt.setInt(1, bookId);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert("Success", "Book " + action + " successfully.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "Book not found or already " + action + ".", Alert.AlertType.ERROR);
            }
            displayBooks(); // Refresh the book list
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to " + action + " the book: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * showAlert
     * This method is used for displaying alert messages to the user, facilitating communication regarding the outcome of
     * various operations within the library management system. It can handle different types of alerts such as success,
     * error, and information. Parameters include the title of the alert, the message to be displayed, and the alert type,
     * which dictates the icon and overall appearance of the alert box. This versatile method ensures that users are
     * adequately informed about the results of their actions, whether it's a confirmation of a successful book checkout,
     * an error message about a failed database operation, or an informational alert about the system's usage. By centralizing
     * the alert presentation logic within this method, the application maintains a consistent user experience across all
     * interactions that require user notification.
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
