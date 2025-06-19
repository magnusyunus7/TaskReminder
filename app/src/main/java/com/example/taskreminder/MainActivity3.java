package com.example.taskreminder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import java.util.List;

public class MainActivity3 extends AppCompatActivity {
    private EditText edit_texttaskname, edit_textdescription;
    private CalendarView calendarview_duedate;
    private Button button_savetask, button_canceltask;
    private TimePicker task_timepicker;
    private int selectedHour, selectedMinute;
    private int selectedYear, selectedMonth, selectedDay;
    private final List<EventDay> taskEvents = new ArrayList<>();
    private boolean isEditMode = false;
    private JSONObject editingTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main3);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edit_texttaskname = findViewById(R.id.editTextTaskName);
        edit_textdescription = findViewById(R.id.editTextDescription);
        calendarview_duedate = findViewById(R.id.calendarViewDueDate);
        button_savetask = findViewById(R.id.buttonSaveTask);
        button_canceltask = findViewById(R.id.buttonCancelTask);
        task_timepicker = findViewById(R.id.taskTimePicker);

        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, 1);
        selectedYear = now.get(Calendar.YEAR);
        selectedMonth = now.get(Calendar.MONTH);
        selectedDay = now.get(Calendar.DAY_OF_MONTH);
        selectedHour = task_timepicker.getHour();
        selectedMinute = task_timepicker.getMinute();

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 1);
        calendarview_duedate.setMinimumDate(minDate);
        calendarview_duedate.setEvents(taskEvents);

        calendarview_duedate.setOnDayClickListener(eventDay -> {
            Calendar clicked = eventDay.getCalendar();
            selectedYear = clicked.get(Calendar.YEAR);
            selectedMonth = clicked.get(Calendar.MONTH);
            selectedDay = clicked.get(Calendar.DAY_OF_MONTH);

            taskEvents.clear();
            taskEvents.add(new EventDay(clicked, R.drawable.ic_event));
            calendarview_duedate.setEvents(new ArrayList<>(taskEvents));

            Toast.makeText(MainActivity3.this,
                    String.format("Selected: %04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay),
                    Toast.LENGTH_SHORT).show();
        });

        task_timepicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
        });

        button_savetask.setOnClickListener(v -> {
            JSONObject task = saveTask();
            if (task != null) {
                setResult(RESULT_OK);
                finish();
            }
        });

        button_canceltask.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent.getBooleanExtra("editMode", false)) {
            isEditMode = true;
            try {
                editingTask = new JSONObject(intent.getStringExtra("taskData"));
                fillFieldsForEditing(editingTask);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillFieldsForEditing(JSONObject task) throws JSONException {
        edit_texttaskname.setText(task.getString("title"));
        edit_textdescription.setText(task.getString("description"));

        String[] parts = task.getString("dueDate").split("-");
        int y = Integer.parseInt(parts[0]);
        int m = Integer.parseInt(parts[1]) - 1;
        int d = Integer.parseInt(parts[2]);
        Calendar c = Calendar.getInstance();
        c.set(y, m, d);
        try {
            calendarview_duedate.setDate(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectedYear = y;
        selectedMonth = m;
        selectedDay = d;

        String[] timeParts = task.getString("dueTime").split(" ")[0].split(":");
        String amPm = task.getString("dueTime").split(" ")[1];
        int hour = Integer.parseInt(timeParts[0]) % 12 + (amPm.equals("PM") ? 12 : 0);
        int minute = Integer.parseInt(timeParts[1]);
        task_timepicker.setHour(hour);
        task_timepicker.setMinute(minute);
        selectedHour = hour;
        selectedMinute = minute;
    }

    private JSONObject saveTask() {
        String title = edit_texttaskname.getText().toString().trim();
        String description = edit_textdescription.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast toast1 = Toast.makeText(this, "Task details incomplete", Toast.LENGTH_SHORT);
            toast1.show();
            new android.os.Handler().postDelayed(toast1::cancel, 1000);
            return null;
        }

        String dueDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
        int displayHour = selectedHour % 12;
        if (displayHour == 0) displayHour = 12;
        String amPm = (selectedHour < 12) ? "AM" : "PM";
        String dueTime = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm);

        SharedPreferences prefs = getSharedPreferences("TaskData", Context.MODE_PRIVATE);
        String tasksJson = prefs.getString("tasks", "[]");

        try {
            JSONArray tasksArray = new JSONArray(tasksJson);

            if (isEditMode && editingTask != null) {
                JSONArray newArray = new JSONArray();
                for (int i = 0; i < tasksArray.length(); i++) {
                    JSONObject task = tasksArray.getJSONObject(i);
                    if (task.optString("title").equals(editingTask.optString("title")) && task.optString("description").equals(editingTask.optString("description")) && task.optString("dueDate").equals(editingTask.optString("dueDate")) && task.optString("dueTime").equals(editingTask.optString("dueTime"))) {
                        continue;
                    }
                    newArray.put(task);
                }
                tasksArray = newArray;
            }

            JSONObject newTask = new JSONObject();
            newTask.put("title", title);
            newTask.put("description", description);
            newTask.put("dueDate", dueDate);
            newTask.put("dueTime", dueTime);
            tasksArray.put(newTask);
            prefs.edit().putString("tasks", tasksArray.toString()).apply();

            Toast toast2 = Toast.makeText(this, "Task saved for " + dueDate, Toast.LENGTH_SHORT);
            toast2.show();
            new android.os.Handler().postDelayed(toast2::cancel, 1500);

            return newTask;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
