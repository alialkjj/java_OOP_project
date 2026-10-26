
import java.util.Scanner;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LibraryProject {
    public static void main(String[] args) {
    Scanner input = new Scanner(System.in);
    Library library = new Library();
    LoanService loanService = new LoanService(library);
    AuthService auth = new AuthService(library);

    try {
        library.addItem(new Book(1,"the art of war","sun tzu"));
        library.addUser(new Librarian(1, "Admin", "admin123", 5001));
        library.addUser(new Member(1001, "Sara", "pass1"));
        library.addItem(new Book(1, "Java Basics", "Smith"));
        library.addItem(new Magazine(2, "Tech Monthly", "TechPress", 42));
        library.addItem(new DVD(3, "Inception", "Nolan"));
    } catch (InvalidInputException e) {
        System.out.println("Setup error: " + e.getMessage());
    }

    new LoginMenu(input, auth, library, loanService).start();
    input.close();
}
}




/* Item classes */

abstract class Item {
    private int id;
    private String title;
    private boolean isAvailable;

    public Item(int id, String title) {
        this.id = id;
        this.title = title;
        this.isAvailable = true;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    public abstract double calculateFee(int daysLate);
    public abstract int getLoanPeriod();

    @Override
    public String toString() {
        return "ID: " + id + ", Title: " + title + ", Available: " + isAvailable;
    }
}

class Book extends Item {
    private String author;

    public Book(int id, String title, String author) {
        super(id, title);
        this.author = author;
    }

    @Override
    public double calculateFee(int daysLate) {
        return daysLate > 0 ? daysLate * 0.5 : 0;
    }

    @Override
    public int getLoanPeriod() { return 14; }

    @Override
    public String toString() {
        return "Book " + super.toString() + ", Author: " + author;
    }
}

class Magazine extends Item {
    private String publisher;
    private int issueNumber;

    public Magazine(int id, String title, String publisher, int issueNumber) {
        super(id, title);
        this.publisher = publisher;
        this.issueNumber = issueNumber;
    }

    @Override
    public double calculateFee(int daysLate) {
        return daysLate > 0 ? daysLate * 0.25 : 0;
    }

    @Override
    public int getLoanPeriod() { return 7; }

    @Override
    public String toString() {
        return "Magazine " + super.toString() + ", Publisher: " + publisher + ", Issue: " + issueNumber;
    }
}



class DVD extends Item {
    private String director;

    public DVD(int id, String title, String director) {
        super(id, title);
        this.director = director;
    }

    @Override
    public double calculateFee(int daysLate) {
        return daysLate > 0 ? daysLate * 1.0 : 0;
    }

    @Override
    public int getLoanPeriod() { return 3; }

    @Override
    public String toString() {
        return "DVD " + super.toString() + ", Director: " + director;
    }
}



/*user classes */

abstract class User {
    private int userId;
    private String name;
    private String password;

    public User(int userId, String name, String password) {
        this.userId = userId;
        this.name = name;
        this.password = password;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public boolean checkPassword(String input) { return password.equals(input); }

    public abstract String getRole();
    public abstract void showDetails();
}


class Librarian extends User {
    private int employeeId;

    public Librarian(int userId, String name, String password, int employeeId) {
        super(userId, name, password);
        this.employeeId = employeeId;
    }

    @Override
    public String getRole() { return "Librarian"; }

    @Override
    public void showDetails() {
        System.out.println("Librarian ID: " + getUserId() + ", Name: " + getName()
                + ", Employee ID: " + employeeId);
    }
}


class Member extends User {
    public static final int MAX_ITEMS = 5;
    private int borrowedCount = 0;
    private double totalFees = 0;

    public Member(int userId, String name, String password) {
        super(userId, name, password);
    }

    public boolean canBorrow() {
        return borrowedCount < MAX_ITEMS && totalFees == 0;
    }

    public void addFine(double amount) { totalFees += amount; }
    public double getTotalFees() { return totalFees; }
    public int getBorrowedCount() { return borrowedCount; }

    @Override
    public String getRole() { return "Member"; }

    @Override
    public void showDetails() {
        System.out.println("Member ID: " + getUserId() + ", Name: " + getName()
                + ", Borrowed: " + borrowedCount + ", Fees: " + totalFees);
    }
    public void addBorrowed() { borrowedCount++; }
public void removeBorrowed() { borrowedCount--; }
}




/* service classes */

class Library {
    private Item[] items = new Item[100];
    private int itemCount = 0;
    private User[] users = new User[100];
    private int userCount = 0;

    public void addItem(Item item) throws InvalidInputException {
        if (findItem(item.getId()) != null) throw new InvalidInputException("Item ID already exists.");
        if (itemCount == items.length) throw new InvalidInputException("Library is full.");
        items[itemCount++] = item;
    }

    public Item findItem(int id) {
        for (int i = 0; i < itemCount; i++) {
            if (items[i].getId() == id) return items[i];
        }
        return null;
    }

    public void removeItem(int id) throws ItemNotAvailableException {
        Item item = findItem(id);
        if (item == null || !item.isAvailable()) {
            throw new ItemNotAvailableException("Item not found or currently borrowed.");
        }
        for (int i = 0; i < itemCount; i++) {
            if (items[i] == item) {
                items[i] = items[itemCount - 1];   // move last item into the gap
                items[--itemCount] = null;
                return;
            }
        }
    }

    public void searchByTitle(String text) {
        boolean found = false;
        for (int i = 0; i < itemCount; i++) {
            if (items[i].getTitle().toLowerCase().contains(text.toLowerCase())) {
                System.out.println(items[i]);
                found = true;
            }
        }
        if (!found) System.out.println("No match found.");
    }

    public void printAllItems() {
        searchByTitle("");   // empty text matches every title
    }

    public void addUser(User user) throws InvalidInputException {
        if (findUser(user.getUserId()) != null) throw new InvalidInputException("User ID already exists.");
        if (userCount == users.length) throw new InvalidInputException("User list is full.");
        users[userCount++] = user;
    }

    public User findUser(int id) {
        for (int i = 0; i < userCount; i++) {
            if (users[i].getUserId() == id) return users[i];
        }
        return null;
    }
}

class Loan {
    Item item;
    int memberId;
    LocalDate borrowDate;
    boolean returned = false;

    Loan(Item item, int memberId) {
        this.item = item;
        this.memberId = memberId;
        this.borrowDate = LocalDate.now();
    }

    int getDaysLate() {
        LocalDate due = borrowDate.plusDays(item.getLoanPeriod());
        long late = ChronoUnit.DAYS.between(due, LocalDate.now());
        return late > 0 ? (int) late : 0;
    }
}

class LoanService {
    private Library library;
    private Loan[] loans = new Loan[200];
    private int loanCount = 0;

    public LoanService(Library library) { this.library = library; }

    public void borrowItem(int memberId, int itemId)
            throws InvalidInputException, ItemNotAvailableException, LimitExceededException {
        User user = library.findUser(memberId);
        if (!(user instanceof Member)) throw new InvalidInputException("Member not found.");
        Member member = (Member) user;

        Item item = library.findItem(itemId);
        if (item == null || !item.isAvailable()) throw new ItemNotAvailableException("Item is not available.");
        if (!member.canBorrow()) throw new LimitExceededException("Borrow limit reached or unpaid fines.");

        item.setAvailable(false);
        member.addBorrowed();
        loans[loanCount++] = new Loan(item, memberId);
    }

    public double returnItem(int memberId, int itemId)
            throws InvalidInputException, ItemNotAvailableException {
        User user = library.findUser(memberId);
        if (!(user instanceof Member)) throw new InvalidInputException("Member not found.");
        Member member = (Member) user;

        Loan loan = findActiveLoan(memberId, itemId);
        if (loan == null) throw new ItemNotAvailableException("No active loan found for this item.");

        double fine = loan.item.calculateFee(loan.getDaysLate());   // polymorphism
        loan.returned = true;
        loan.item.setAvailable(true);
        member.removeBorrowed();
        member.addFine(fine);
        return fine;
    }

    private Loan findActiveLoan(int memberId, int itemId) {
        for (int i = 0; i < loanCount; i++) {
            if (!loans[i].returned && loans[i].memberId == memberId
                    && loans[i].item.getId() == itemId) return loans[i];
        }
        return null;
    }
}

class AuthService {
    private Library library;

    public AuthService(Library library) {
        this.library = library;
    }

    public User login(int id, String password) throws InvalidInputException {
        User user = library.findUser(id);
        if (user == null || !user.checkPassword(password)) {
            throw new InvalidInputException("Wrong ID or password.");
        }
        return user;   // can be a Librarian or a Member
    }
}

/* exceptions */

class ItemNotAvailableException extends Exception {
    public ItemNotAvailableException(String message) { super(message); }
}

class InvalidInputException extends Exception {
    public InvalidInputException(String message) { super(message); }
}

class LimitExceededException extends Exception {
    public LimitExceededException(String message) { super(message); }
}

/*UI */


class LoginMenu {
    private Scanner input;
    private AuthService auth;
    private Library library;
    private LoanService loanService;

    public LoginMenu(Scanner input, AuthService auth, Library library, LoanService loanService) {
        this.input = input;
        this.auth = auth;
        this.library = library;
        this.loanService = loanService;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Library System ===");
            System.out.println("1. Login");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            try {
                switch (InputValidator.readInt(input)) {
                    case 1: login(); break;
                    case 0: running = false; break;
                    default: System.out.println("Invalid option.");
                }
            } catch (InvalidInputException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void login() throws InvalidInputException {
        System.out.print("User ID: ");
        int id = InputValidator.readPositiveInt(input);
        System.out.print("Password: ");
        String password = input.nextLine();

        User user = auth.login(id, password);
        System.out.println("Welcome, " + user.getName() + " (" + user.getRole() + ")");

        if (user instanceof Librarian) {
            new LibrarianMenu(input, library).show((Librarian) user);
        } else {
            new MemberMenu(input, library, loanService).show((Member) user);
        }
    }
}

class LibrarianMenu {
    private Scanner input;
    private Library library;

    public LibrarianMenu(Scanner input, Library library) {
        this.input = input;
        this.library = library;
    }

    public void show(Librarian librarian) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Librarian Menu (" + librarian.getName() + ") ===");
            System.out.println("1. Add item");
            System.out.println("2. Remove item");
            System.out.println("3. View all items");
            System.out.println("4. Add member");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            try {
                switch (InputValidator.readInt(input)) {
                    case 1: addItem(); break;
                    case 2: removeItem(); break;
                    case 3: library.printAllItems(); break;
                    case 4: addMember(); break;
                    case 0: running = false; break;
                    default: System.out.println("Invalid option.");
                }
            } catch (InvalidInputException | ItemNotAvailableException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void addItem() throws InvalidInputException {
        System.out.println("1. Book  2. Magazine  3. DVD");
        System.out.print("Type: ");
        int type = InputValidator.readInt(input);
        System.out.print("ID: ");
        int id = InputValidator.readPositiveInt(input);
        System.out.print("Title: ");
        String title = InputValidator.readNonEmpty(input, "Title");

        Item item;
        switch (type) {
            case 1:
                System.out.print("Author: ");
                item = new Book(id, title, InputValidator.readNonEmpty(input, "Author"));
                break;
            case 2:
                System.out.print("Publisher: ");
                String publisher = InputValidator.readNonEmpty(input, "Publisher");
                System.out.print("Issue number: ");
                item = new Magazine(id, title, publisher, InputValidator.readPositiveInt(input));
                break;
            case 3:
                System.out.print("Director: ");
                item = new DVD(id, title, InputValidator.readNonEmpty(input, "Director"));
                break;
            default:
                throw new InvalidInputException("Unknown item type.");
        }
        library.addItem(item);
        System.out.println("Item added.");
    }

    private void removeItem() throws InvalidInputException, ItemNotAvailableException {
        System.out.print("Item ID to remove: ");
        library.removeItem(InputValidator.readPositiveInt(input));
        System.out.println("Item removed.");
    }

    private void addMember() throws InvalidInputException {
        System.out.print("Member ID: ");
        int id = InputValidator.readPositiveInt(input);
        System.out.print("Name: ");
        String name = InputValidator.readNonEmpty(input, "Name");
        System.out.print("Password: ");
        String password = InputValidator.readNonEmpty(input, "Password");
        library.addUser(new Member(id, name, password));   // addUser, not addMember
        System.out.println("Member added.");
    }
}

class MemberMenu {
    private Scanner input;
    private Library library;
    private LoanService loanService;

    public MemberMenu(Scanner input, Library library, LoanService loanService) {
        this.input = input;
        this.library = library;
        this.loanService = loanService;
    }

    public void show(Member member) {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Member Menu (" + member.getName() + ") ===");
            System.out.println("1. Search catalog");
            System.out.println("2. Borrow item");
            System.out.println("3. Return item");
            System.out.println("4. My details and fines");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            try {
                switch (InputValidator.readInt(input)) {
                    case 1:
                        System.out.print("Title to search: ");
                        library.searchByTitle(InputValidator.readNonEmpty(input, "Search text"));
                        break;
                    case 2:
                        System.out.print("Item ID to borrow: ");
                        loanService.borrowItem(member.getUserId(), InputValidator.readPositiveInt(input));
                        System.out.println("Item borrowed.");
                        break;
                    case 3:
                        System.out.print("Item ID to return: ");
                        double fine = loanService.returnItem(member.getUserId(), InputValidator.readPositiveInt(input));
                        System.out.println("Item returned. Fine: " + fine);
                        break;
                    case 4: member.showDetails(); break;
                    case 0: running = false; break;
                    default: System.out.println("Invalid option.");
                }
            } catch (InvalidInputException | ItemNotAvailableException | LimitExceededException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}

/* utility classes */

class InputValidator {
    public static int readInt(Scanner in) throws InvalidInputException {
        String line = in.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Please enter a valid number.");
        }
    }

    public static int readPositiveInt(Scanner in) throws InvalidInputException {
        int value = readInt(in);
        if (value <= 0) {
            throw new InvalidInputException("Value must be greater than zero.");
        }
        return value;
    }

    public static String readNonEmpty(Scanner in, String fieldName) throws InvalidInputException {
        String text = in.nextLine().trim();
        if (text.isEmpty()) {
            throw new InvalidInputException(fieldName + " cannot be empty.");
        }
        return text;
    }
}

