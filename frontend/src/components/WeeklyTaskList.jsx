import React from "react";

function WeeklyTaskList({ tasks }) {
  console.log("Tasks from backend:", tasks);

  return (
    <div className="weekly-task-list-panel">
      <div className="weekly-panel-header">
        <h2>All Tasks</h2>
      </div>

      <div className="weekly-task-list-scroll">
        {tasks.length === 0 ? (
          <p className="empty-message">No tasks available.</p>
        ) : (
          tasks.map((task, index) => (
            <div key={task.id ?? index} className="weekly-task-card">
              <h3>{task.name || "Untitled Task"}</h3>

              <div className="weekly-task-meta">
                <span><strong>Type:</strong> {task.taskType ?? "N/A"}</span>
                <span><strong>Due:</strong> {task.dueDate ?? "N/A"}</span>
                <span><strong>Estimated Time:</strong> {task.estimatedTime ?? "N/A"} hr</span>
                <span><strong>Grade Weight:</strong> {task.gradeWeight ?? "N/A"}%</span>
                <span><strong>Current Grade:</strong> {task.currentGrade ?? "N/A"}</span>
                <span><strong>Priority Score:</strong> {task.priorityScore ?? "N/A"}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default WeeklyTaskList;