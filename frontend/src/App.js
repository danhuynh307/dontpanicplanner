import React, { useState } from "react";
import { Routes, Route } from "react-router-dom";

import DashboardCards from "./components/DashboardCards";
import CalendarView from "./components/CalendarView";
import CreateTaskPanel from "./components/CreateTaskPanel";
import TaskListPanel from "./components/TaskListPanel";

import WeeklyAvailability from "./pages/WeeklyAvailability";
import WeeklySchedule from "./pages/WeeklySchedule";

import "./styles/app.css";

function Dashboard() {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [tasks, setTasks] = useState([]);

  const addTask = (newTask) => {
    setTasks([...tasks, newTask]);
  };

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