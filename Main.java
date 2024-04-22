import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileInputStream;
import javafx.scene.image.Image;

public class Main extends Application {

    static final String JDBC_DRIVER = "org.postgresql.Driver";
    static final String DB_URL = "jdbc:postgresql://localhost/gymfit";
    static final String USER = "postgres";
    static final String PASS = "2802";

    private Connection conn = null;
    private Statement stmt = null;
    private int balance = 5000;
    private List<String> myCourses = new ArrayList<>();
    private Map<String, String> videoLinks = new HashMap<>();
    private Map<String, String> courseImages = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        initVideoLinks();
        initCourseImages();

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("Course Search");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label balanceLabel = new Label("Balance: " + balance + " KZT");
        balanceLabel.setStyle("-fx-font-size: 16px;");

        Image courseImage = new Image(new FileInputStream("C:/Users/opste/Desktop/coursepage.jpg"));
        ImageView courseImageView = new ImageView(courseImage);

        ComboBox<String> courseComboBox = new ComboBox<>();
        courseComboBox.getItems().addAll("Maths", "HistoryKZ", "English");
        courseComboBox.setPromptText("Select Course");

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(200);

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> {
            String selectedCourse = courseComboBox.getValue();
            if (selectedCourse != null) {
                searchCourse(selectedCourse, resultArea);
            } else {
                resultArea.setText("Please select a course.");
            }
        });

        Button sortByAscendingButton = new Button("Sort Ascending");
        sortByAscendingButton.setOnAction(e -> {
            resultArea.setText("");
            String selectedCourse = courseComboBox.getValue();
            if (selectedCourse != null) {
                searchCourse(selectedCourse, resultArea, "ASC");
            }
        });

        Button sortByDescendingButton = new Button("Sort Descending");
        sortByDescendingButton.setOnAction(e -> {
            resultArea.setText("");
            String selectedCourse = courseComboBox.getValue();
            if (selectedCourse != null) {
                searchCourse(selectedCourse, resultArea, "DESC");
            }
        });

        Button buyButton = new Button("Buy");
        buyButton.setOnAction(e -> {
            String selectedCourse = courseComboBox.getValue();
            if (selectedCourse != null) {
                int coursePrice = getPrice(selectedCourse);
                if (balance >= coursePrice) {
                    balance -= coursePrice;
                    balanceLabel.setText("Balance: " + balance + " KZT");
                    resultArea.setText("Course bought successfully!");
                    myCourses.add(selectedCourse);
                } else {
                    resultArea.setText("Not enough balance to buy this course.");
                }
            } else {
                resultArea.setText("Please select a course.");
            }
        });

        Button myCoursesButton = new Button("My Courses");
        myCoursesButton.setOnAction(e -> showMyCourses());

        HBox buttonsBox = new HBox(10);
        buttonsBox.getChildren().addAll(searchButton, sortByAscendingButton, sortByDescendingButton);

        HBox buyBox = new HBox(10);
        buyBox.getChildren().addAll(buyButton, myCoursesButton);

        root.getChildren().addAll(titleLabel, balanceLabel, courseImageView, courseComboBox, buttonsBox, buyBox, resultArea);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("UNT preparation courses");
        primaryStage.show();

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected to the database");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initVideoLinks() {
        videoLinks.put("Maths", "https://www.youtube.com/@da_vinci_center");
        videoLinks.put("HistoryKZ", "https://www.youtube.com/watch?v=aR_ihLRxt-Y&list=PLl4GqiUOpSnPP6lQXnCJ9OoHPoqKwpbhB");
        videoLinks.put("English", "https://www.youtube.com/watch?v=Hp9wUEDasY4&list=PLD6SPjEPomaustGSgYNsn3V62BTQeH85X");
    }

    private void initCourseImages() {
        courseImages.put("Maths", "C:/Users/opste/Desktop/maths.jpg");
        courseImages.put("HistoryKZ", "C:/Users/opste/Desktop/historyKZ.jpg");
        courseImages.put("English", "C:/Users/opste/Desktop/english.jpg");
    }

    private void searchCourse(String courseName, TextArea resultArea) {
        searchCourse(courseName, resultArea, "ASC");
    }

    private void searchCourse(String courseName, TextArea resultArea, String sortOrder) {
        try {
            String sql = "SELECT * FROM " + courseName + " ORDER BY price " + sortOrder;
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            StringBuilder result = new StringBuilder();
            while (rs.next()) {
                String chapter = rs.getString("chapter");
                int duration = rs.getInt("Duration_day");
                int price = rs.getInt("price");
                result.append("Chapter: ").append(chapter).append(", Duration: ").append(duration).append(" days, Price: ").append(price).append("\n");
            }

            if (result.length() == 0) {
                resultArea.setText("No courses found.");
            } else {
                resultArea.setText(result.toString());
            }

            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private int getPrice(String courseName) {
        try {
            String sql = "SELECT price FROM " + courseName + " LIMIT 1";
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                int price = rs.getInt("price");
                rs.close();
                stmt.close();
                return price;
            } else {
                rs.close();
                stmt.close();
                return -1;
            }
        } catch (SQLException se) {
            se.printStackTrace();
            return -1;
        }
    }

    private void showMyCourses() {
        VBox coursesBox = new VBox(10);
        coursesBox.setPadding(new Insets(10));

        if (myCourses.isEmpty()) {
            coursesBox.getChildren().add(new Label("You haven't bought any courses yet."));
        } else {
            for (String course : myCourses) {
                try {
                    String imagePath = courseImages.get(course);
                    Image image = new Image(new FileInputStream(imagePath));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(100); // Adjust width
                    imageView.setFitHeight(100); // Adjust height
                    Hyperlink videoLink = new Hyperlink("Watch Video");
                    videoLink.setOnAction(e -> getHostServices().showDocument(videoLinks.get(course)));
                    VBox courseBox = new VBox(10);
                    courseBox.getChildren().addAll(new Label(course), imageView, videoLink);
                    coursesBox.getChildren().add(courseBox);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Stage myCoursesStage = new Stage();
        myCoursesStage.setTitle("My Courses");
        myCoursesStage.setScene(new Scene(coursesBox, 400, 400));
        myCoursesStage.show();
    }

    @Override
    public void stop() throws Exception {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
