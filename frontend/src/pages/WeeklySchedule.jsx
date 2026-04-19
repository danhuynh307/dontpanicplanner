import React, { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import WeeklyTaskList from "../components/WeeklyTaskList";
import WeeklyCalendarView from "../components/WeeklyCalendarView";
import { fetchWeeklySchedule } from "../services/scheduleService";
import { getTasks, deleteTask, updateTask } from "../services/taskService";
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

  const handleRemoveTask = async (taskId) => {
    try {
      await deleteTask(taskId);
      await loadPageData();
    } catch (error) {
      console.error("Failed to delete task:", error);
    }
  };

  const handleDeleteFromCalendar = async (block) => {
    try {
      const originalTaskName = block.taskTitle.replace(/\s*\(Part \d+\/\d+\)$/, "");

      const matchingTask = tasks.find((task) => task.name === originalTaskName);

      if (!matchingTask) {
        console.error("Could not find matching task for block:", block);
        return;
      }

      await deleteTask(matchingTask.id);
      await loadPageData();
    } catch (error) {
      console.error("Failed to delete task from calendar:", error);
    }
  };

  // saves edited task fields to the backend then reloads
  const handleUpdateTask = async (taskId, updatedData) => {
    try {
      await updateTask(taskId, updatedData);
      await loadPageData();
    } catch (error) {
      console.error("Failed to update task:", error);
    }
  };

  const handleUpdateSchedule = async () => {
    await loadPageData();
  };

  return (
    <div className="weekly-schedule-wrapper">
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
              tasks={tasks}
              onPrevWeek={handlePrevWeek}
              onNextWeek={handleNextWeek}
              onDeleteBlock={handleDeleteFromCalendar}
            />
          </>
        )}
      </div>
    </div>
  );
}

export default WeeklySchedule;