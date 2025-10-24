import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotesManager {
    private static final String NOTES_DIRECTORY = "notes";
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        createNotesDirectory();
        
        System.out.println("=== TEXT-BASED NOTES MANAGER ===");
        
        while (true) {
            displayMenu();
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    createNewNote();
                    break;
                case "2":
                    viewAllNotes();
                    break;
                case "3":
                    readNote();
                    break;
                case "4":
                    deleteNote();
                    break;
                case "5":
                    searchNotes();
                    break;
                case "6":
                    System.out.println("Thank you for using Notes Manager. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void displayMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Create New Note");
        System.out.println("2. View All Notes");
        System.out.println("3. Read Note");
        System.out.println("4. Delete Note");
        System.out.println("5. Search Notes");
        System.out.println("6. Exit");
        System.out.print("Enter your choice (1-6): ");
    }
    
    private static void createNotesDirectory() {
        File directory = new File(NOTES_DIRECTORY);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                System.out.println("Notes directory created successfully.");
            } else {
                System.out.println("Failed to create notes directory.");
            }
        }
    }
    
    private static void createNewNote() {
        System.out.println("\n--- CREATE NEW NOTE ---");
        System.out.print("Enter note title: ");
        String title = scanner.nextLine().trim();
        
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty!");
            return;
        }
        
        // Create filename from title (replace spaces with underscores and remove special chars)
        String filename = title.toLowerCase().replaceAll("[^a-z0-9\\-\\s]", "").replaceAll("\\s+", "_") + ".txt";
        String filepath = NOTES_DIRECTORY + File.separator + filename;
        
        File file = new File(filepath);
        if (file.exists()) {
            System.out.println("A note with this title already exists!");
            return;
        }
        
        System.out.println("Enter your note content (type 'END' on a new line to finish):");
        StringBuilder content = new StringBuilder();
        
        // Add metadata
        content.append("Title: ").append(title).append("\n");
        content.append("Created: ").append(LocalDateTime.now().format(formatter)).append("\n");
        content.append("--- Content ---\n");
        
        // Read content until user types END
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append("\n");
        }
        
        // Write to file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content.toString());
            System.out.println("Note saved successfully as: " + filename);
        } catch (IOException e) {
            System.out.println("Error saving note: " + e.getMessage());
        }
    }
    
    private static void viewAllNotes() {
        System.out.println("\n--- ALL NOTES ---");
        File directory = new File(NOTES_DIRECTORY);
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        
        if (files == null || files.length == 0) {
            System.out.println("No notes found.");
            return;
        }
        
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        for (int i = 0; i < files.length; i++) {
            System.out.printf("%d. %s (Last modified: %s)%n", 
                i + 1, 
                files[i].getName().replace(".txt", "").replace("_", " "),
                new Date(files[i].lastModified()));
        }
    }
    
    private static void readNote() {
        System.out.println("\n--- READ NOTE ---");
        File[] files = getNoteFiles();
        
        if (files.length == 0) {
            System.out.println("No notes available to read.");
            return;
        }
        
        displayNoteList(files);
        System.out.print("Enter note number to read: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > files.length) {
                System.out.println("Invalid note number!");
                return;
            }
            
            File selectedFile = files[choice - 1];
            readNoteFromFile(selectedFile);
            
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }
    
    private static void readNoteFromFile(File file) {
        System.out.println("\n--- " + file.getName().replace(".txt", "").replace("_", " ") + " ---");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading note: " + e.getMessage());
        }
    }
    
    private static void deleteNote() {
        System.out.println("\n--- DELETE NOTE ---");
        File[] files = getNoteFiles();
        
        if (files.length == 0) {
            System.out.println("No notes available to delete.");
            return;
        }
        
        displayNoteList(files);
        System.out.print("Enter note number to delete: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > files.length) {
                System.out.println("Invalid note number!");
                return;
            }
            
            File selectedFile = files[choice - 1];
            System.out.print("Are you sure you want to delete '" + selectedFile.getName() + "'? (y/n): ");
            String confirmation = scanner.nextLine();
            
            if (confirmation.equalsIgnoreCase("y")) {
                if (selectedFile.delete()) {
                    System.out.println("Note deleted successfully.");
                } else {
                    System.out.println("Failed to delete note.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }
    
    private static void searchNotes() {
        System.out.println("\n--- SEARCH NOTES ---");
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine().toLowerCase();
        
        File[] files = getNoteFiles();
        List<File> matchingFiles = new ArrayList<>();
        
        for (File file : files) {
            if (file.getName().toLowerCase().contains(searchTerm.replace(" ", "_"))) {
                matchingFiles.add(file);
                continue;
            }
            
            // Search in file content
            if (searchInFileContent(file, searchTerm)) {
                matchingFiles.add(file);
            }
        }
        
        if (matchingFiles.isEmpty()) {
            System.out.println("No notes found matching: " + searchTerm);
        } else {
            System.out.println("Found " + matchingFiles.size() + " note(s) matching '" + searchTerm + "':");
            for (int i = 0; i < matchingFiles.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, 
                    matchingFiles.get(i).getName().replace(".txt", "").replace("_", " "));
            }
        }
    }
    
    private static boolean searchInFileContent(File file, String searchTerm) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(searchTerm)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Ignore read errors for search
        }
        return false;
    }
    
    private static File[] getNoteFiles() {
        File directory = new File(NOTES_DIRECTORY);
        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        return files != null ? files : new File[0];
    }
    
    private static void displayNoteList(File[] files) {
        for (int i = 0; i < files.length; i++) {
            System.out.printf("%d. %s%n", i + 1, 
                files[i].getName().replace(".txt", "").replace("_", " "));
        }
    }
}