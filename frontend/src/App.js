import React, { useEffect, useState } from "react";
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
import { getTasks, createTask } from "./services/taskService";

import "./styles/app.css";
// contains calendar and create task widget
function Dashboard() {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [tasks, setTasks] = useState([]);

  useEffect(() => {
    loadTasks();
  }, []);
    // load task from data structure
  const loadTasks = async () => {
    try {
      const data = await getTasks();
      setTasks(data);
    } catch (error) {
      console.error("Failed to load tasks:", error);
    }
  };
    // add task widget connecting
  const addTask = async (newTask) => {
    try {
      const savedTask = await createTask(newTask);
      setTasks((prevTasks) => [...prevTasks, savedTask]);
    } catch (error) {
      console.error("Failed to create task:", error);
    }
  };
    //webpage layout
  return (
    <div className="app-container">
      <DashboardCards />

      <div className="main-content">
        <CalendarView
          selectedDate={selectedDate}
          setSelectedDate={setSelectedDate}
        />

        <div className="right-panel">
          <CreateTaskPanel addTask={addTask} />
          <TaskListPanel tasks={tasks} selectedDate={selectedDate} />
        </div>
      </div>
    </div>
  );
}
    // running the app and webpages
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