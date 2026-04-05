import React, { useEffect, useState } from "react";

// user inputted form
function CreateTaskPanel({ addTask, selectedDate }) {
  const formatDateToYYYYMMDD = (date) => {
    if (!date) return "";
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const today = formatDateToYYYYMMDD(new Date());

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

    if (formData.dueDate && formData.dueDate < today) {
      alert("Due date cannot be in the past.");
      return;
    }

    const cleanedData = {
      ...formData,
      estimatedTime: Number(formData.estimatedTime),
      gradeWeight: Number(formData.gradeWeight),
      currentGrade: Number(formData.currentGrade),
    };

    await addTask(cleanedData);

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
      <h3>Create Task</h3>

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
    </div>
  );
}

export default CreateTaskPanel;