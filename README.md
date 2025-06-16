# TaskReminder — Android Task & Deadline Reminder App

TaskReminder is a clean, lightweight Android app designed to help users track upcoming tasks and deadlines. It features a minimal interface, intuitive calendar views, and smart input for due dates and times — making it a perfect personal productivity companion.

---

## Features

- **Task Creation**  
  Add tasks with titles, descriptions, due dates, and time using a user-friendly form.

- **Calendar Integration**  
  Visualize all your upcoming tasks in a calendar view using [Material CalendarView](https://github.com/Applandeo/Material-Calendar-View).

- **Time Picker**  
  Set task time with Android's native `TimePicker`.

- **View Tasks by Date**  
  Tap a calendar date to see all tasks scheduled for that day.

- **Delete Tasks**  
  Select and remove individual tasks with confirmation.

- **Data Persistence**  
  Stores task data locally using `SharedPreferences` in JSON format.

---

## App Flow

MainActivity (Home Screen)

└── MainActivity2 (Menu)

├── MainActivity3 (Add Task)

└── MainActivity4 (View Tasks Calendar)

---

## Tech Stack

- Java (Android SDK)
- [MaterialCalendarView](https://github.com/Applandeo/Material-Calendar-View)
- SharedPreferences for data storage
- Android XML Layouts with `EdgeToEdge` & WindowInsets handling

---

## How to Build

- **Clone the repo**

`git clone https://github.com/yourusername/TaskReminder.git`

`cd TaskReminder`

- **Open in Android Studio**

(File → Open → select the project folder)

- **Run on an emulator or a physical device**

---

## Generate APK

In Android Studio:
- **Go to Build → Build Bundle(s) / APK(s) → Build APK(s)**

- **Locate your APK at:**

`app/build/outputs/apk/debug/app-debug.apk`

---

## License

This project is licensed under the Apache License.

---
