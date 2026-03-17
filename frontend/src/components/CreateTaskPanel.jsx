import React, { useState } from "react";

function CreateTaskPanel({ addTask }) {
  const [formData, setFormData] = useState({
    title: "",
    type: "Assignment",
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

  const handleSubmit = (e) => {
    e.preventDefault();
    addTask(formData);

    setFormData({
      title: "",
      type: "Assignment",
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
          name="title"
          placeholder="Task title"
          value={formData.title}
          onChange={handleChange}
        />

        <label>Task Type</label>
        <select
          name="type"
          value={formData.type}
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