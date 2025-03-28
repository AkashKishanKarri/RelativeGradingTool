import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

// Base Class (Inheritance)
class Person {
    String name;
    
    Person(String name) {
        this.name = name;
    }
    
    // Polymorphism (Overriding)
    public String toString() {
        return "Name: " + name;
    }
}

// Derived Class (Inheritance + Polymorphism)
class Student extends Person {
    double marks;
    String grade;
    
    Student(String name, double marks) {
        super(name);  // Calls Parent Constructor
        this.marks = marks;
    }
    
    // Overriding (Polymorphism)
    @Override
    public String toString() {
        return super.toString() + ", Marks: " + marks + ", Grade: " + grade;
    }
}

// Interface for Grading System
interface Grading {
    void calculateStatistics();
    void assignGrades();
    void saveResultsToFile(String filePath) throws IOException;
}

// Grading System Implementation (Polymorphism using Interface)
class GradingSystem implements Grading {
    List<Student> students = new ArrayList<>();
    double mean, stdDev;
    Map<String, Integer> gradeDistribution = new LinkedHashMap<>();
    Map<String, List<Student>> gradeWiseStudents = new LinkedHashMap<>();
    
    // File Handling (Reading Data)
    void loadFromCSV(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        br.readLine();  // Skip Header
        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            double marks = Double.parseDouble(data[1].trim());
            students.add(new Student(data[0].trim(), marks));
        }
        br.close();
    }
    
    // Calculate Mean and Standard Deviation
    @Override
    public void calculateStatistics() {
        double sum = 0;
        for (Student s : students) sum += s.marks;
        mean = sum / students.size();
        
        double variance = 0;
        for (Student s : students) variance += Math.pow(s.marks - mean, 2);
        stdDev = Math.sqrt(variance / students.size());
    }
    
    // Assign Grades Based on Mean & Std Dev
    @Override
    public void assignGrades() {
        for (Student s : students) {
            s.grade = getGrade(s.marks);
            gradeDistribution.put(s.grade, gradeDistribution.getOrDefault(s.grade, 0) + 1);
            gradeWiseStudents.computeIfAbsent(s.grade, k -> new ArrayList<>()).add(s);
        }
    }
    
    String getGrade(double marks) {
        if (marks >= mean + 2.0 * stdDev) return "O"; 
        if (marks >= mean + 1.2 * stdDev) return "A+";
        if (marks >= mean + 0.5 * stdDev) return "A";
        if (marks >= mean - 0.2 * stdDev) return "B+";
        if (marks >= mean - 1.5 * stdDev) return "B";
        return "Fail";
    }
    
    // Save Results to File
    @Override
    public void saveResultsToFile(String filePath) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
        bw.write("Name, Marks, Grade\n");
        for (Student s : students) {
            bw.write(s.name + ", " + s.marks + ", " + s.grade + "\n");
        }
        bw.close();
        System.out.println("Results saved successfully.");
    }
}

// GUI for Grade Visualization
class GradeVisualizer extends JPanel {
    private final GradingSystem gradingSystem;
    private final Map<String, Rectangle> gradeBars = new LinkedHashMap<>();

    GradeVisualizer(GradingSystem gradingSystem) {
        this.gradingSystem = gradingSystem;
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                for (Map.Entry<String, Rectangle> entry : gradeBars.entrySet()) {
                    if (entry.getValue().contains(e.getPoint())) {
                        showStudentDetails(entry.getKey());
                    }
                }
            }
        });
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = 50;
        int maxHeight = 300;
        int barWidth = 80;

        int maxValue = gradingSystem.gradeDistribution.values().stream().max(Integer::compare).orElse(1);

        for (Map.Entry<String, Integer> entry : gradingSystem.gradeDistribution.entrySet()) {
            String grade = entry.getKey();
            int count = entry.getValue();
            int barHeight = (int) ((count / (double) maxValue) * maxHeight);
            
            g.setColor(new Color(70, 130, 180));
            g.fillRect(x, getHeight() - barHeight - 60, barWidth, barHeight);
            g.setColor(Color.BLACK);
            g.drawRect(x, getHeight() - barHeight - 60, barWidth, barHeight);
            
            g.drawString(grade, x + 25, getHeight() - 40);
            g.drawString(String.valueOf(count), x + 35, getHeight() - barHeight - 65);
            
            gradeBars.put(grade, new Rectangle(x, getHeight() - barHeight - 60, barWidth, barHeight));
            x += 100;
        }
    }
    
    private void showStudentDetails(String grade) {
        List<Student> students = gradingSystem.gradeWiseStudents.get(grade);
        if (students == null || students.isEmpty()) return;
        
        StringBuilder message = new StringBuilder("Students with grade " + grade + ":\n");
        for (Student s : students) {
            message.append(s.name).append(" - ").append(s.marks).append(" marks\n");
        }
        JOptionPane.showMessageDialog(this, message.toString(), "Grade Details", JOptionPane.INFORMATION_MESSAGE);
    }
}

// Main Application
public class AdvancedGradingApp {
    public static void main(String[] args) throws IOException {
        String filePath = JOptionPane.showInputDialog("Enter CSV file path:");
        if (filePath == null || filePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No file provided!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Grading gradingSystem = new GradingSystem(); // Polymorphism
        ((GradingSystem) gradingSystem).loadFromCSV(filePath);
        gradingSystem.calculateStatistics();
        gradingSystem.assignGrades();
        gradingSystem.saveResultsToFile("grades_output.csv");
        
        JFrame frame = new JFrame("Grade Distribution");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new GradeVisualizer((GradingSystem) gradingSystem));
        frame.setVisible(true);
    }
}