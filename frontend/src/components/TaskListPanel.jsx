import React from "react";

function TaskListPanel({ tasks, selectedDate }) {
  return (
    <div className="task-list-panel">
      <div className="task-list-header">
        <h3>Select a date</h3>
        <button>Show All</button>
      </div>

      <div className="task-list-content">
        {tasks.length === 0 ? (
          <p>Select a date or show all tasks</p>
        ) : (
          tasks.map((task, index) => (
            <div key={index} className="task-card">
              <h4>{task.name}</h4>
              <p>Type: {task.taskType}</p>
              <p>Due: {task.dueDate}</p>
              <p>Time: {task.estimatedTime} hrs</p>
              <p>Weight: {task.gradeWeight}%</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default TaskListPanel;