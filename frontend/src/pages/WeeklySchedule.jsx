import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import WeeklyTaskList from "../components/WeeklyTaskList";
import WeeklyCalendarView from "../components/WeeklyCalendarView";
import { fetchWeeklySchedule } from "../services/scheduleService";
import { getTasks } from "../services/taskService";
import "../styles/WeeklySchedule.css";

function WeeklySchedule() {
  const navigate = useNavigate();

  const getStartOfWeek = (date) => {
    const d = new Date(date);
    const day = d.getDay();
    d.setDate(d.getDate() - day);
    d.setHours(0, 0, 0, 0);
    return d;
  };

  const formatDateToYYYYMMDD = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const [weekStartDate, setWeekStartDate] = useState(getStartOfWeek(new Date()));
  const [tasks, setTasks] = useState([]);
  const [scheduleBlocks, setScheduleBlocks] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadPageData = async () => {
    try {
      setLoading(true);

      const [taskData, scheduleData] = await Promise.all([
        getTasks(),
        fetchWeeklySchedule(formatDateToYYYYMMDD(weekStartDate)),
      ]);

      setTasks(taskData || []);
      setScheduleBlocks(scheduleData.blocks || []);
    } catch (error) {
      console.error("Error loading weekly schedule page:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPageData();
  }, [weekStartDate]);

  const handlePrevWeek = () => {
    const newDate = new Date(weekStartDate);
    newDate.setDate(newDate.getDate() - 7);
    setWeekStartDate(newDate);
  };

  const handleNextWeek = () => {
    const newDate = new Date(weekStartDate);
    newDate.setDate(newDate.getDate() + 7);
    setWeekStartDate(newDate);
  };

  // Stubs — wire up to real handlers later
  const handleRemoveTask = (taskIndex) => {
    console.log("Remove task at index:", taskIndex);
  };

  const handleUpdateTask = (taskIndex) => {
    console.log("Update task at index:", taskIndex);
  };

  const handleUpdateSchedule = async () => {
    await loadPageData();
  };

  return (
    <div className="weekly-schedule-wrapper">
      {/* GMU-style top bar */}
      <div className="schedule-top-bar">
        <button className="schedule-back-btn" onClick={() => navigate("/")}>
          &#8592; Back
        </button>
        <div className="schedule-top-bar-title">
          <span className="schedule-top-bar-label">Don't Panic Planner</span>
          <span className="schedule-top-bar-sep">|</span>
          <span>Weekly Schedule</span>
        </div>
        <button className="schedule-update-btn" onClick={handleUpdateSchedule}>
          Update Schedule
        </button>
      </div>

      <div className="weekly-schedule-page">
        {loading ? (
          <div className="weekly-loading">Loading weekly schedule...</div>
        ) : (
          <>
            <WeeklyTaskList
              tasks={tasks}
              onRemoveTask={handleRemoveTask}
              onUpdateTask={handleUpdateTask}
            />
            <WeeklyCalendarView
              weekStartDate={weekStartDate}
              blocks={scheduleBlocks}
              onPrevWeek={handlePrevWeek}
              onNextWeek={handleNextWeek}
            />
          </>
        )}
      </div>
    </div>
  );
}

export default WeeklySchedule;