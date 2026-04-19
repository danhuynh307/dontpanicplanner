import React, { useState } from "react";

// returns the css class for the priority badge based on score
function priorityBadgeClass(score) {
  if (score == null) return "priority-badge priority-na";
  if (score >= 75) return "priority-badge priority-high";
  if (score >= 50) return "priority-badge priority-med";
  return "priority-badge priority-low";
}

function WeeklyTaskList({ tasks, onRemoveTask, onUpdateTask }) {
  const [editingTaskId, setEditingTaskId] = useState(null);
  const [editData, setEditData] = useState({});

  // opens the inline edit form pre-filled with the task's current values
  const startEdit = (task) => {
    setEditingTaskId(task.id);
    setEditData({
      name:          task.name          ?? "",
      taskType:      task.taskType      ?? "Assignment",
      estimatedTime: task.estimatedTime ?? 1,
      dueDate:       task.dueDate       ?? "",
      gradeWeight:   task.gradeWeight   ?? 10,
      currentGrade:  task.currentGrade  ?? 0,
    });
  };

  const handleEditChange = (e) => {
    setEditData({ ...editData, [e.target.name]: e.target.value });
  };

  // submits the edited values and closes the form
  const handleSave = () => {
    if (onUpdateTask) onUpdateTask(editingTaskId, editData);
        setEditingTaskId(null);
  };

  const handleCancel = () => setEditingTaskId(null);

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
              <div className="weekly-task-card-header">
                <h3>{task.name || "Untitled Task"}</h3>
                <span className={priorityBadgeClass(task.priorityScore)}>
                  {task.priorityScore != null ? Math.round(task.priorityScore) : "—"}
                </span>
              </div>

              {/* inline edit form only shown for the card being edited */}
              {editingTaskId === task.id ? (
                <div className="task-edit-form">
                  <label>Name</label>
                  <input name="name" value={editData.name} onChange={handleEditChange} />

                  <label>Type</label>
                  <select name="taskType" value={editData.taskType} onChange={handleEditChange}>
                    <option>Assignment</option>
                    <option>Study</option>
                    <option>Essay</option>
                    <option>Project</option>
                  </select>

                  <label>Est. Time (hrs)</label>
                  <input type="number" name="estimatedTime" value={editData.estimatedTime} onChange={handleEditChange} min="0" step="0.5" />

                  <label>Due Date</label>
                  <input type="date" name="dueDate" value={editData.dueDate} onChange={handleEditChange} />

                  <label>Grade Weight (%)</label>
                  <input type="number" name="gradeWeight" value={editData.gradeWeight} onChange={handleEditChange} min="0" />

                  <label>Current Grade (%)</label>
                  <input type="number" name="currentGrade" value={editData.currentGrade} onChange={handleEditChange} min="0" />

                  <div className="task-edit-actions">
                    <button className="task-btn task-btn-update" onClick={handleSave}>Save</button>
                    <button className="task-btn task-btn-cancel" onClick={handleCancel}>Cancel</button>
                  </div>
                </div>
              ) : (
                <>
                  <div className="weekly-task-meta">
                    <span><strong>Type:</strong> {task.taskType ?? "N/A"}</span>
                    <span><strong>Due:</strong> {task.dueDate ?? "N/A"}</span>
                    <span><strong>Est. Time:</strong> {task.estimatedTime ?? "N/A"} hr</span>
                    <span><strong>Grade Weight:</strong> {task.gradeWeight ?? "N/A"}%</span>
                    <span><strong>Current Grade:</strong> {task.currentGrade ?? "N/A"}</span>
                  </div>

                  <div className="weekly-task-actions">
                    <button className="task-btn task-btn-update" onClick={() => startEdit(task)}>
                      Update Task
                    </button>
                    <button className="task-btn task-btn-remove" onClick={() => onRemoveTask && onRemoveTask(task.id)}>
                      Remove
                    </button>
                  </div>
                </>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default WeeklyTaskList;
