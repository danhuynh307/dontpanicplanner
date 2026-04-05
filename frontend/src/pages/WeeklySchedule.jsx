import React, { useEffect, useState } from "react";
import WeeklyTaskList from "../components/WeeklyTaskList";
import WeeklyCalendarView from "../components/WeeklyCalendarView";
import { fetchWeeklySchedule } from "../services/scheduleService";
import { getTasks } from "../services/taskService";
import "../styles/WeeklySchedule.css";

function WeeklySchedule() {
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

  return (
    <div className="weekly-schedule-page">
      {loading ? (
        <div className="weekly-loading">Loading weekly schedule...</div>
      ) : (
        <>
          <WeeklyTaskList tasks={tasks} />
          <WeeklyCalendarView
            weekStartDate={weekStartDate}
            blocks={scheduleBlocks}
            onPrevWeek={handlePrevWeek}
            onNextWeek={handleNextWeek}
          />
        </>
      )}
    </div>
  );
}

export default WeeklySchedule;