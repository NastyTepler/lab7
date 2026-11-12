package app;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Book;
import model.Visitor;

import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.List;

public class DatabaseTask {

    private static final String URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";

    public static void run() {
        try (Connection conn = DriverManager.getConnection(URL, "sa", "")) {

            // схема study
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE SCHEMA IF NOT EXISTS study;");

                // таблица music
                stmt.execute("CREATE TABLE IF NOT EXISTS study.music (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(255) NOT NULL);");

                stmt.execute("INSERT INTO study.music (title) VALUES ('Bohemian Rhapsody'), ('Hotel California'), ('Stairway to Heaven'), ('Imagine'), ('Shape of You');");
            }

            System.out.println("Задание 1: Список музыкальных композиций");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM study.music")) {
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + " - " + rs.getString("title"));
                }
            }

            System.out.println("\nЗадание 2: Фильтрация (без 'm' и 't')");
            String sqlFilter = "SELECT * FROM study.music WHERE LOWER(title) NOT LIKE '%m%' AND LOWER(title) NOT LIKE '%t%'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlFilter)) {
                while (rs.next()) {
                    System.out.println(rs.getString("title"));
                }
            }

            System.out.println("\nЗадание 3: Добавление композиции");
            String sqlInsert = "INSERT INTO study.music (title) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setString(1, "My Favorite Song");
                ps.executeUpdate();
                System.out.println("Добавлена песня 'My Favorite Song'");
            }

            // Задание 4: Загрузка из JSON
            System.out.println("\nЗадание 4: Загрузка из JSON");
            loadFromJson(conn);

            System.out.println("\nЗадание 5: Сортировка книг");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM study.books ORDER BY publishing_year")) {
                while (rs.next()) {
                    System.out.println(rs.getInt("publishing_year") + " - " + rs.getString("name"));
                }
            }

            System.out.println("\nЗадание 6: Книги до 2000 года");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM study.books WHERE publishing_year < 2000")) {
                while (rs.next()) {
                    System.out.println(rs.getString("name") + " (" + rs.getInt("publishing_year") + ")");
                }
            }

            // Задание 7: Добавление себя и книг
            System.out.println("\nЗадание 7: Добавление себя");
            addMyself(conn);

            // Задание 8: Удаление таблиц
            System.out.println("\nЗадание 8: Удаление таблиц");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS study.books");
                stmt.execute("DROP TABLE IF EXISTS study.visitors");
                System.out.println("Таблицы visitors и books удалены.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadFromJson(Connection conn) throws Exception {
        Gson gson = new Gson();
        try (Reader reader = new FileReader("data/books.json")) {
            Type listType = new TypeToken<List<Visitor>>(){}.getType();
            List<Visitor> visitors = gson.fromJson(reader, listType);

            // Создаем таблицы
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS study.visitors (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), phone VARCHAR(50) UNIQUE)");
                stmt.execute("CREATE TABLE IF NOT EXISTS study.books (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255) UNIQUE, author VARCHAR(255), publishing_year INT)");
            }

            // Вставляем данные без дубликатов
            String sqlVisitor = "MERGE INTO study.visitors (name, phone) KEY(phone) VALUES (?, ?)";
            String sqlBook = "MERGE INTO study.books (name, author, publishing_year) KEY(name) VALUES (?, ?, ?)";

            try (PreparedStatement psV = conn.prepareStatement(sqlVisitor);
                 PreparedStatement psB = conn.prepareStatement(sqlBook)) {

                for (Visitor v : visitors) {
                    psV.setString(1, v.getName() + " " + v.getSurname());
                    psV.setString(2, v.getPhone());
                    psV.executeUpdate();

                    if (v.getFavoriteBooks() != null) {
                        for (Book b : v.getFavoriteBooks()) {
                            psB.setString(1, b.getName());
                            psB.setString(2, b.getAuthor());
                            psB.setInt(3, b.getPublishingYear());
                            psB.executeUpdate();
                        }
                    }
                }
            }
            System.out.println("Данные из JSON успешно загружены в базу.");
        }
    }

    private static void addMyself(Connection conn) throws Exception {
        String myName = "Теплякова Анастасия";
        String myPhone = "+79990000000";

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO study.visitors (name, phone) VALUES (?, ?)")) {
            ps.setString(1, myName);
            ps.setString(2, myPhone);
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO study.books (name, author, publishing_year) VALUES (?, ?, ?)")) {
            ps.setString(1, "Мастер и Маргарита");
            ps.setString(2, "Михаил Булгаков");
            ps.setInt(3, 1967);
            ps.executeUpdate();
        }

        System.out.println("Добавлены данные о себе и любимой книге.");

        System.out.println("Чтение из базы:");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM study.visitors")) {
            while (rs.next()) {
                System.out.println("Посетитель: " + rs.getString("name") + " | Тел: " + rs.getString("phone"));
            }
        }
    }
}