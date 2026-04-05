import React, { useMemo, useState, useEffect } from "react";
import CalendarView from "../components/CalendarView";
import CreateTaskPanel from "../components/CreateTaskPanel";
import TaskListPanel from "../components/TaskListPanel";
import { getTasks, createTask } from "../services/taskService";

function WeeklySchedule() {
  const [tasks, setTasks] = useState([]);
  const [selectedDate, setSelectedDate] = useState(null);
  const [showAllTasks, setShowAllTasks] = useState(false);

  useEffect(() => {
    async function loadTasks() {
      try {
        const data = await getTasks();
        setTasks(data);
      } catch (error) {
        console.error("Failed to load tasks:", error);
      }
    }

    loadTasks();
  }, []);

  const normalizeDate = (value) => {
    if (!value) return "";

    if (typeof value === "string") {
      if (value.includes("T")) {
        return value.split("T")[0];
      }

      if (value.includes("/")) {
        const [month, day, year] = value.split("/");
        return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
      }

      return value;
    }

    if (typeof value === "object" && value.year && value.month && value.day) {
      return `${value.year}-${String(value.month).padStart(2, "0")}-${String(value.day).padStart(2, "0")}`;
    }

    if (value instanceof Date) {
      const year = value.getFullYear();
      const month = String(value.getMonth() + 1).padStart(2, "0");
      const day = String(value.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    }

    return "";
  };

  const handleAddTask = async (taskData) => {
    try {
      const createdTask = await createTask(taskData);
      setTasks((prevTasks) => [...prevTasks, createdTask]);
    } catch (error) {
      console.error("Failed to create task:", error);
    }
  };

  const handleSelectDate = (dateObj) => {
    setSelectedDate(dateObj);
    setShowAllTasks(false);
  };

  const displayedTasks = useMemo(() => {
    if (showAllTasks) return tasks;
    if (!selectedDate) return [];

    const selectedDateString = normalizeDate(selectedDate);

    return tasks.filter((task) => {
      const taskDateString = normalizeDate(task.dueDate);
      return taskDateString === selectedDateString;
    });
  }, [tasks, selectedDate, showAllTasks]);

  return (
    <div className="weekly-schedule">
      <CalendarView
        selectedDate={selectedDate}
        setSelectedDate={handleSelectDate}
      />

      <CreateTaskPanel addTask={handleAddTask} />

      <TaskListPanel
        tasks={displayedTasks}
        selectedDate={selectedDate}
        showAllTasks={showAllTasks}
        onToggleShowAll={() => setShowAllTasks((prev) => !prev)}
      />
    </div>
  );
}

export default WeeklySchedule;