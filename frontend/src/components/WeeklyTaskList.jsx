import React from "react";

function priorityBadgeClass(score) {
  if (score == null) return "priority-badge priority-na";
  if (score >= 75) return "priority-badge priority-high";
  if (score >= 50) return "priority-badge priority-med";
  return "priority-badge priority-low";
}

function WeeklyTaskList({ tasks, onRemoveTask, onUpdateTask }) {
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
              {/* Card top row: name + priority badge */}
              <div className="weekly-task-card-header">
                <h3>{task.name || "Untitled Task"}</h3>
                <span className={priorityBadgeClass(task.priorityScore)}>
                  {task.priorityScore != null
                    ? Math.round(task.priorityScore)
                    : "—"}
                </span>
              </div>

              <div className="weekly-task-meta">
                <span><strong>Type:</strong> {task.taskType ?? "N/A"}</span>
                <span><strong>Due:</strong> {task.dueDate ?? "N/A"}</span>
                <span><strong>Est. Time:</strong> {task.estimatedTime ?? "N/A"} hr</span>
                <span><strong>Grade Weight:</strong> {task.gradeWeight ?? "N/A"}%</span>
                <span><strong>Current Grade:</strong> {task.currentGrade ?? "N/A"}</span>
              </div>

              {/* Per-task action buttons */}
              <div className="weekly-task-actions">
                <button
                  className="task-btn task-btn-update"
                  onClick={() => onUpdateTask && onUpdateTask(index)}
                >
                  Update Task
                </button>
                <button
                  className="task-btn task-btn-remove"
                  onClick={() => onRemoveTask && onRemoveTask(index)}
                >
                  Remove
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default WeeklyTaskList;