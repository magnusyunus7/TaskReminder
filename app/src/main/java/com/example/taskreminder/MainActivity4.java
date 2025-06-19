package com.example.taskreminder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity4 extends AppCompatActivity {
    private Button button_goback;
    private CalendarView calendarview_upcoming;
    private ActivityResultLauncher<Intent> editTaskLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main4);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTaskLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        recreate();
                    }
                }
        );

        button_goback = findViewById(R.id.buttonGoBack);
        button_goback.setOnClickListener(v -> finish());

        calendarview_upcoming = findViewById(R.id.calendarViewUpcoming);
        SharedPreferences prefs = getSharedPreferences("TaskData", Context.MODE_PRIVATE);
        String tasksJson = prefs.getString("tasks", "[]");

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        calendarview_upcoming.setMinimumDate(today);

        List<EventDay> events = new ArrayList<>();
        Map<String, List<JSONObject>> tasksByDate = new HashMap<>();

        try {
            JSONArray tasksArray = new JSONArray(tasksJson);
            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject task = tasksArray.getJSONObject(i);
                String dueDateStr = task.getString("dueDate");
                String[] parts = dueDateStr.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1;
                int day = Integer.parseInt(parts[2]);

                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day);

                events.add(new EventDay(cal, R.drawable.ic_event));
                tasksByDate.computeIfAbsent(dueDateStr, k -> new ArrayList<>()).add(task);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        calendarview_upcoming.setEvents(events);
        calendarview_upcoming.setSwipeEnabled(false);
        calendarview_upcoming.setOnDayClickListener(eventDay -> {
            Calendar clicked = eventDay.getCalendar();
            String key = String.format("%04d-%02d-%02d",
                    clicked.get(Calendar.YEAR),
                    clicked.get(Calendar.MONTH) + 1,
                    clicked.get(Calendar.DAY_OF_MONTH));

            List<JSONObject> dayTasks = tasksByDate.get(key);
            if (dayTasks != null && !dayTasks.isEmpty()) {
                SpannableStringBuilder message = new SpannableStringBuilder();

                for (int i = 0; i < dayTasks.size(); i++) {
                    JSONObject t = dayTasks.get(i);

                    message.append("Task ").append(String.valueOf(i + 1)).append(":\n");
                    message.append("• Task name: ").append(t.optString("title")).append("\n");
                    message.append("  Description: ").append(t.optString("description")).append("\n");
                    message.append("  Time: ").append(t.optString("dueTime")).append("\n\n");
                }

                TextView msgView = new TextView(MainActivity4.this);
                msgView.setText(message);
                msgView.setPadding(40, 30, 40, 30);
                msgView.setTextSize(16);
                msgView.setMovementMethod(new ScrollingMovementMethod());

                new AlertDialog.Builder(MainActivity4.this)
                        .setTitle("Tasks due " + key)
                        .setView(msgView)
                        .setPositiveButton("Close", null)
                        .setNegativeButton("Edit Task", (dialog, which) -> showEditTaskDialog(dayTasks))
                        .setNeutralButton("Delete Task", (dialog, which) -> showDeleteTaskDialog(dayTasks))
                        .show();
            }
            else {
                Toast.makeText(MainActivity4.this, "No tasks due on this date", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteTaskDialog(List<JSONObject> dayTasks) {
        String[] taskTitles = new String[dayTasks.size()];
        for (int i = 0; i < dayTasks.size(); i++) {
            JSONObject t = dayTasks.get(i);
            taskTitles[i] = t.optString("title") + " (" + t.optString("dueTime") + ")";
        }

        new AlertDialog.Builder(MainActivity4.this)
                .setTitle("Select task to delete")
                .setItems(taskTitles, (dialog, which) -> {
                    JSONObject selectedTask = dayTasks.get(which);
                    new AlertDialog.Builder(MainActivity4.this)
                            .setTitle("Confirm Delete")
                            .setMessage("Delete task \"" + selectedTask.optString("title") + "\"?")
                            .setPositiveButton("Yes", (d, w) -> performDeleteTask(selectedTask))
                            .setNegativeButton("No", null)
                            .show();
                })
                .show();
    }


    private void performDeleteTask(JSONObject taskToDelete) {
        SharedPreferences prefs = getSharedPreferences("TaskData", Context.MODE_PRIVATE);
        String tasksJson = prefs.getString("tasks", "[]");
        try {
            JSONArray tasksArray = new JSONArray(tasksJson);
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject task = tasksArray.getJSONObject(i);
                if (!(task.optString("title").equals(taskToDelete.optString("title")) &&
                        task.optString("description").equals(taskToDelete.optString("description")) &&
                        task.optString("dueDate").equals(taskToDelete.optString("dueDate")) &&
                        task.optString("dueTime").equals(taskToDelete.optString("dueTime")))) {
                    newArray.put(task);
                }
            }
            prefs.edit().putString("tasks", newArray.toString()).apply();
            Toast.makeText(this, "Task deleted.", Toast.LENGTH_SHORT).show();
            recreate();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showEditTaskDialog(List<JSONObject> dayTasks) {
        String[] taskTitles = new String[dayTasks.size()];
        for (int i = 0; i < dayTasks.size(); i++) {
            JSONObject t = dayTasks.get(i);
            taskTitles[i] = t.optString("title") + " (" + t.optString("dueTime") + ")";
        }

        new AlertDialog.Builder(MainActivity4.this)
                .setTitle("Select task to edit")
                .setItems(taskTitles, (dialog, which) -> {
                    JSONObject toEdit = dayTasks.get(which);
                    Intent edit = new Intent(MainActivity4.this, MainActivity3.class);
                    edit.putExtra("editMode", true);
                    edit.putExtra("taskData", toEdit.toString());
                    editTaskLauncher.launch(edit);
                })
                .show();
    }
}
