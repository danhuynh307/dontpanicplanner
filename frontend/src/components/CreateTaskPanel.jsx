import React, { useState } from "react";

function CreateTaskPanel({ addTask }) {
  const [formData, setFormData] = useState({
    name: "",
    taskType: "Assignment",
    estimatedTime: 1,
    dueDate: "",
    gradeWeight: 10,
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const cleanedData = {
      ...formData,
      estimatedTime: Number(formData.estimatedTime),
      gradeWeight: Number(formData.gradeWeight),
    };

    await addTask(cleanedData);

    setFormData({
      name: "",
      taskType: "Assignment",
      estimatedTime: 1,
      dueDate: "",
      gradeWeight: 10,
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
          <option>Exam</option>
          <option>Project</option>
        </select>

        <label>Estimated Time (hours)</label>
        <input
          type="number"
          name="estimatedTime"
          value={formData.estimatedTime}
          onChange={handleChange}
        />

        <label>Due Date</label>
        <input
          type="date"
          name="dueDate"
          value={formData.dueDate}
          onChange={handleChange}
        />

        <label>Grade Weight (%)</label>
        <input
          type="number"
          name="gradeWeight"
          value={formData.gradeWeight}
          onChange={handleChange}
        />

        <button type="submit">Add Task</button>
      </form>
    </div>
  );
}

export default CreateTaskPanel;