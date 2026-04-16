import React from "react";
import "../styles/app.css";
import { getTasks, deleteTask } from "../services/taskService";

function TaskListPanel({tasks, showAllTasks, onToggleShowAll, onDeleteTask,}) {
  return (
    <div className="task-list-panel">
      <div className="task-list-header">
        <h3>Tasks</h3>
        <button onClick={onToggleShowAll}>
          {showAllTasks ? "Show Selected Date Only" : "Show All Tasks"}
        </button>
      </div>

      {tasks.length === 0 ? (
        <p>No tasks yet</p>
      ) : (
        tasks.map((task, i) => {
          const score = task.priorityScore;
          const badgeClass =
            score == null ? "priority-badge priority-na"
            : score >= 75  ? "priority-badge priority-high"
            : score >= 50  ? "priority-badge priority-med"
            :                "priority-badge priority-low";

          return (
            <div key={i} className="task-card">
              <div className="task-card-header">
                <div>
                  <h4>{task.name}</h4>
                  <p className="task-card-type">{task.taskType}</p>
                </div>
                <div className="task-card-right">
                  <span className={badgeClass}>
                    {score != null ? Math.round(score) : "—"}
                  </span>
                  <button onClick={() => onDeleteTask(task)} className="delete-button">
                    Delete
                  </button>
                </div>
              </div>

              <div className="task-card-meta">
                <span>Due: {task.dueDate || "No due date"}</span>
                <span>{task.estimatedTime} hrs</span>
                <span>{task.gradeWeight}% weight</span>
              </div>
            </div>
          );
        })
      )}
    </div>
  );
}

export default TaskListPanel;