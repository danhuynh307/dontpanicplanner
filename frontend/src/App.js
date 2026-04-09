import React, { useEffect, useMemo, useState } from "react";
import { Routes, Route } from "react-router-dom";
//components
import DashboardCards from "./components/DashboardCards";
import CalendarView from "./components/CalendarView";
import CreateTaskPanel from "./components/CreateTaskPanel";
import TaskListPanel from "./components/TaskListPanel";
//pages
import WeeklyAvailability from "./pages/WeeklyAvailability";
import WeeklySchedule from "./pages/WeeklySchedule";
//services
import { getTasks, createTask, exportTasksCSV, importTasksCSV } from "./services/taskService";

import "./styles/app.css";

function Dashboard() {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [tasks, setTasks] = useState([]);
  const [showAllTasks, setShowAllTasks] = useState(false);

  useEffect(() => {
    loadTasks();
  }, []);

  const loadTasks = async () => {
    try {
      const data = await getTasks();
      setTasks(data);
    } catch (error) {
      console.error("Failed to load tasks:", error);
    }
  };

  const addTask = async (newTask) => {
    try {
      const savedTask = await createTask(newTask);
      setTasks((prevTasks) => [...prevTasks, savedTask]);
    } catch (error) {
      console.error("Failed to create task:", error);
    }
  };

  const formatDateToYYYYMMDD = (date) => {
    if (!date) return "";
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const displayedTasks = useMemo(() => {
    if (showAllTasks) return tasks;
    if (!selectedDate) return [];

    const selectedDateString = formatDateToYYYYMMDD(selectedDate);
    return tasks.filter((task) => task.dueDate === selectedDateString);
  }, [tasks, selectedDate, showAllTasks]);

  const handleSelectDate = (dateObj) => {
    setSelectedDate(dateObj);
    setShowAllTasks(false);
  };

  const deleteTask = (taskIndex) => {
    setTasks((prevTasks) => prevTasks.filter((_, index) => index !== taskIndex));
  };

  // downloads all tasks as a CSV file
  const handleExport = async () => {
    try {
      await exportTasksCSV();
    } catch (error) {
      console.error("Export failed:", error);
    }
  };

  // uploads a CSV file, imports the tasks, then refreshes the task list
  const handleImport = async (file) => {
    try {
      await importTasksCSV(file);
      await loadTasks();
    } catch (error) {
      console.error("Import failed:", error);
    }
  };

  return (
    <div className="dashboard-wrapper">
      {/* GMU top bar */}
      <div className="dashboard-top-bar">
        <div className="dashboard-top-bar-left">
          <span className="dashboard-top-bar-brand">Don't Panic Planner</span>
          <span className="dashboard-top-bar-sep">|</span>
          <span className="dashboard-top-bar-page">Dashboard</span>
        </div>

        <div className="dashboard-top-bar-actions">
          {/* hidden file input triggered by Import button */}
          <input
            id="csv-import-input"
            type="file"
            accept=".csv"
            style={{ display: "none" }}
            onChange={(e) => {
              if (e.target.files[0]) handleImport(e.target.files[0]);
              e.target.value = "";
            }}
          />
          <button
            className="topbar-csv-btn topbar-csv-import"
            onClick={() => document.getElementById("csv-import-input").click()}
          >
            &#8593; Import CSV
          </button>
          <button className="topbar-csv-btn topbar-csv-export" onClick={handleExport}>
            &#8595; Export CSV
          </button>
        </div>
      </div>

      <div className="app-container">
      <DashboardCards />

      <div className="main-content">
        <CalendarView
          selectedDate={selectedDate}
          setSelectedDate={handleSelectDate}
          tasks={tasks}
        />

        <div className="right-panel">
          <CreateTaskPanel
            addTask={addTask}
            selectedDate={selectedDate}
          />
          <TaskListPanel
            tasks={displayedTasks}
            selectedDate={selectedDate}
            showAllTasks={showAllTasks}
            onToggleShowAll={() => setShowAllTasks((prev) => !prev)}
            onDeleteTask={deleteTask}
          />
        </div>
      </div>
      </div>
    </div>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<Dashboard />} />
      <Route path="/availability" element={<WeeklyAvailability />} />
      <Route path="/schedule" element={<WeeklySchedule />} />
    </Routes>
  );
}

export default App;