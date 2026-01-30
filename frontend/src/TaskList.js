import React, { useEffect, useState } from 'react';
import axios from 'axios';

function TaskList() {
    const [tasks, setTasks] = useState([]);

    useEffect(() => {
        axios.get('http://localhost:8080/tasks')
            .then(res => setTasks(res.data))
            .catch(err => console.error(err));
    }, []);

    return (
        <div>
            <h2>Tasks</h2>
            {tasks.length === 0 ? (
                <p>No tasks yet.</p>
            ) : (
                <ul>
                    {tasks.map(task => (
                        <li key={task.id}>{task.name} - Due: {task.dueDate}</li>
                    ))}
                </ul>
            )}
        </div>
    );
}

export default TaskList;
