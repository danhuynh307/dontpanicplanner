import React from "react";

function TaskListPanel({
  tasks,
  showAllTasks,
  onToggleShowAll,
  onDeleteTask,
}) {
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
        tasks.map((task, i) => (
          <div key={i} className="task-card">
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <div>
                <h4>{task.name}</h4>
                <p>{task.taskType}</p>
              </div>

              <button
                onClick={() => onDeleteTask(i)}
                style={{
                  background: "#e74c3c",
                  color: "white",
                  border: "none",
                  borderRadius: "8px",
                  padding: "6px 10px",
                  cursor: "pointer",
                  fontSize: "12px",
                }}
              >
                Delete
              </button>
            </div>

            <div style={{ marginTop: "8px" }}>
              <p>Due: {task.dueDate || "No due date"}</p>
              <p>{task.estimatedTime} hrs</p>
              <p>{task.gradeWeight}%</p>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default TaskListPanel;