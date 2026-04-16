import React, { useEffect, useState } from "react";

function CreateTaskPanel({ addTask, selectedDate, untitledCount, setUntitledCount }) {
  const formatDateToYYYYMMDD = (date) => {
    if (!date) return "";
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const today = formatDateToYYYYMMDD(new Date());

  const [isOpen, setIsOpen] = useState(false);

  const [formData, setFormData] = useState({
    name: "",
    taskType: "Assignment",
    estimatedTime: 1,
    dueDate: formatDateToYYYYMMDD(selectedDate),
    gradeWeight: 10,
    currentGrade: 0,
  });

  useEffect(() => {
    setFormData((prevFormData) => ({
      ...prevFormData,
      dueDate: formatDateToYYYYMMDD(selectedDate),
    }));
  }, [selectedDate]);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (Number(formData.estimatedTime) < 0) {
      alert("Estimated time cannot be below 0.");
      return;
    }

    if (Number(formData.estimatedTime) % 0.5 !== 0) {
        alert("Estimated time must be in 0.5 hour increments (0.5, 1, 1.5...).");
        return;
    }

    if (formData.dueDate && formData.dueDate < today) {
      alert("Due date cannot be in the past.");
      return;
    }

    let taskName = formData.name.trim();
        if (taskName === "") {
          taskName = `Task ${untitledCount}`;
          setUntitledCount((prev) => prev + 1);
        }

    const cleanedData = {
      ...formData,
      name: taskName,
      estimatedTime: Number(formData.estimatedTime),
      gradeWeight: Number(formData.gradeWeight),
      currentGrade: Number(formData.currentGrade),
    };

    await addTask(cleanedData);

    // reset form
    setFormData({
      name: "",
      taskType: "Assignment",
      estimatedTime: 1,
      dueDate: formatDateToYYYYMMDD(selectedDate),
      gradeWeight: 10,
      currentGrade: 0,
    });
  };

  return (
    <div className="task-panel">
      <h3
        onClick={() => setIsOpen(!isOpen)}
        style={{ cursor: "pointer" }}
      >
        Create Task {isOpen ? "▲" : "▼"}
      </h3>

      {isOpen && (
        <form onSubmit={handleSubmit} className="task-form">
          <label>Task Name</label>
          <input
            type="text"
            name="name"
            placeholder="Task title"
            value={formData.name}
            onChange={handleChange}
          />

          <label>Task Type</label>
          <select
            name="taskType"
            value={formData.taskType}
            onChange={handleChange}
          >
            <option>Assignment</option>
            <option>Study</option>
            <option>Essay</option>
            <option>Project</option>
          </select>

          <label>Estimated Time (hours)</label>
          <input
            type="number"
            name="estimatedTime"
            value={formData.estimatedTime}
            onChange={handleChange}
            min="0"
            step="0.5"
          />

          <label>Due Date</label>
          <input
            type="date"
            name="dueDate"
            value={formData.dueDate}
            onChange={handleChange}
            min={today}
          />

          <label>Grade Weight (%)</label>
          <input
            type="number"
            name="gradeWeight"
            value={formData.gradeWeight}
            onChange={handleChange}
          />

          <label>Current Grade (%)</label>
          <input
            type="number"
            name="currentGrade"
            value={formData.currentGrade}
            onChange={handleChange}
            min="0"
          />

          <button type="submit">Add Task</button>
        </form>
      )}
    </div>
  );
}

export default CreateTaskPanel;