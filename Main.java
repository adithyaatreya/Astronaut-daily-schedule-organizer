import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The Task class represents a single task for an astronaut's daily schedule.
 * It includes task description, start and end times, priority, and completion status.
 */
class Task {
    private String description;
    private Date startTime;
    private Date endTime;
    private String priority;
    private boolean isCompleted;
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    /**
     * Constructor to create a task with description, start time, end time, and priority.
     * Throws ParseException if the time format is invalid.
     */
    public Task(String description, String startTime, String endTime, String priority) throws ParseException {
        this.description = description;
        this.startTime = validateTime(startTime);
        this.endTime = validateTime(endTime);
        this.priority = priority;
        this.isCompleted = false;
    }

    // Validate time format (HH:MM)
    private Date validateTime(String timeStr) throws ParseException {
        try {
            return timeFormat.parse(timeStr);
        } catch (ParseException e) {
            throw new ParseException("Error: Invalid time format. Please use HH:MM format.", 0);
        }
    }

    // Getters for task properties
    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    // Mark the task as completed
    public void markCompleted() {
        this.isCompleted = true;
    }

    // Edit the details of an existing task
    public void editTask(String newDescription, String newStartTime, String newEndTime, String newPriority) throws ParseException {
        this.description = newDescription;
        this.startTime = validateTime(newStartTime);
        this.endTime = validateTime(newEndTime);
        this.priority = newPriority;
    }

    @Override
    public String toString() {
        String status = isCompleted ? "[Completed]" : "[Pending]";
        return timeFormat.format(startTime) + " - " + timeFormat.format(endTime) + ": " + description + " [" + priority + "] " + status;
    }
}

/**
 * TaskFactory is responsible for creating Task objects.
 * It ensures that task creation follows a standardized process.
 */
class TaskFactory {
    public static Task createTask(String description, String startTime, String endTime, String priority) throws ParseException {
        return new Task(description, startTime, endTime, priority);
    }
}

/**
 * Singleton class to manage all tasks in the astronaut's daily schedule.
 * It ensures only one instance of the ScheduleManager is created.
 */
class ScheduleManager {
    private static ScheduleManager instance;
    private List<Task> tasks;
    private List<ConflictObserver> observers;

    // Private constructor to enforce Singleton pattern
    private ScheduleManager() {
        tasks = new ArrayList<>();
        observers = new ArrayList<>();
    }

    // Get the Singleton instance of ScheduleManager
    public static ScheduleManager getInstance() {
        if (instance == null) {
            instance = new ScheduleManager();
        }
        return instance;
    }

    // Add an observer to notify about task conflicts
    public void addObserver(ConflictObserver observer) {
        observers.add(observer);
    }

    // Notify observers about task conflicts
    private void notifyObservers(Task task) {
        for (ConflictObserver observer : observers) {
            observer.update(task);
        }
    }

    /**
     * Add a new task to the schedule.
     * If a task conflicts with an existing one, the observer is notified.
     */
    public void addTask(Task task) {
        if (isConflicting(task)) {
            System.out.println("Error: Task conflicts with existing task.");
            notifyObservers(task);
        } else {
            tasks.add(task);
            tasks.sort(Comparator.comparing(Task::getStartTime)); // Sort tasks by start time
            System.out.println("Task added successfully: " + task.getDescription() + ".");
        }
    }

    /**
     * Remove a task from the schedule by its description.
     * If no task matches the description, an error message is shown.
     */
    public void removeTask(String description) {
        Task task = tasks.stream().filter(t -> t.getDescription().equals(description)).findFirst().orElse(null);
        if (task != null) {
            tasks.remove(task);
            System.out.println("Task '" + description + "' removed successfully.");
        } else {
            System.out.println("Error: Task '" + description + "' not found.");
        }
    }

    /**
     * Edit an existing task by its description.
     * The task's details (description, times, and priority) can be modified.
     */
    public void editTask(String oldDescription, String newDescription, String newStartTime, String newEndTime, String newPriority) {
        Task task = tasks.stream().filter(t -> t.getDescription().equals(oldDescription)).findFirst().orElse(null);
        if (task != null) {
            try {
                task.editTask(newDescription, newStartTime, newEndTime, newPriority);
                tasks.sort(Comparator.comparing(Task::getStartTime)); // Re-sort after editing times
                System.out.println("Task edited successfully.");
            } catch (ParseException e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Error: Task '" + oldDescription + "' not found.");
        }
    }

    /**
     * Mark a task as completed by its description.
     * If no task matches the description, an error message is shown.
     */
    public void markTaskCompleted(String description) {
        Task task = tasks.stream().filter(t -> t.getDescription().equals(description)).findFirst().orElse(null);
        if (task != null) {
            task.markCompleted();
            System.out.println("Task '" + description + "' marked as completed.");
        } else {
            System.out.println("Error: Task '" + description + "' not found.");
        }
    }

    /**
     * View tasks that match a specific priority level.
     * If no tasks match the priority, an informative message is shown.
     */
    public void viewTasksByPriority(String priority) {
        List<Task> filteredTasks = tasks.stream().filter(t -> t.getPriority().equalsIgnoreCase(priority)).collect(Collectors.toList());
        if (filteredTasks.isEmpty()) {
            System.out.println("No tasks with priority: " + priority);
        } else {
            for (Task task : filteredTasks) {
                System.out.println(task);
            }
        }
    }

    /**
     * View all tasks in the schedule sorted by their start time.
     * If no tasks are scheduled, an informative message is shown.
     */
    public void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks scheduled for the day.");
        } else {
            for (Task task : tasks) {
                System.out.println(task);
            }
        }
    }

    // Check if the new task conflicts with any existing tasks
    private boolean isConflicting(Task newTask) {
        for (Task task : tasks) {
            if (!(newTask.getEndTime().before(task.getStartTime()) || newTask.getStartTime().after(task.getEndTime()))) {
                return true;
            }
        }
        return false;
    }
}

/**
 * Logger class provides simple logging functionality for the application.
 * It can be extended for more complex logging (e.g., file logging).
 */
class Logger {
    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

/**
 * ConflictObserver class implements the Observer pattern.
 * It gets notified when task conflicts occur.
 */
class ConflictObserver {
    public void update(Task task) {
        System.out.println("Conflict detected with task: " + task.getDescription());
    }
}

/**
 * Main class to run the console-based Astronaut Daily Schedule Organizer.
 * It demonstrates adding, removing, editing, viewing tasks, and handling task conflicts.
 */
public class Main {
    public static void main(String[] args) {
        // Get the Singleton instance of ScheduleManager
        ScheduleManager manager = ScheduleManager.getInstance();
        TaskFactory factory = new TaskFactory();
        Logger logger = new Logger();

        // Adding an observer to notify about task conflicts
        ConflictObserver observer = new ConflictObserver();
        manager.addObserver(observer);

        try {
            // Adding some tasks to the schedule
            Task task1 = factory.createTask("Morning Exercise", "07:00", "08:00", "High");
            manager.addTask(task1);

            Task task2 = factory.createTask("Team Meeting", "09:00", "10:00", "Medium");
            manager.addTask(task2);

            // Demonstrating task conflict (Observer will notify)
            Task task3 = factory.createTask("Training Session", "09:30", "10:30", "High");
            manager.addTask(task3);  // This will trigger conflict notification

            // Editing a task
            manager.editTask("Morning Exercise", "Morning Walk", "07:00", "08:00", "Low");

            // Marking a task as completed
            manager.markTaskCompleted("Team Meeting");

            // Viewing all tasks
            manager.viewTasks();

            // Viewing tasks by priority
            manager.viewTasksByPriority("High");

            // Removing a task
            manager.removeTask("Morning Walk");
            manager.viewTasks();

        } catch (ParseException e) {
            logger.log(e.getMessage());
        }
    }
}
