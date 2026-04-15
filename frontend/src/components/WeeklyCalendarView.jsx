import React from "react";

const DAYS = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
const START_HOUR = 6;
const END_HOUR = 21;
const HOUR_HEIGHT = 64; // px per hour

// distinct calendar-style colors, one per unique task
const BLOCK_COLORS = [
  "#8b5cf6", // purple
  "#3b82f6", // blue
  "#16a34a", // green
  "#f97316", // orange
  "#ec4899", // pink
  "#0891b2", // cyan
  "#d97706", // amber
  "#ef4444", // red
  "#7c3aed", // violet
  "#0d9488", // teal
];

// builds a stable task-key → color map from all blocks on the page
function buildColorMap(blocks) {
  const keys = [];
  blocks.forEach((block) => {
    const base = block.taskTitle?.replace(/\s*\(Part \d+\/\d+\)$/, "") ?? "";
    const key  = block.taskId != null ? String(block.taskId) : base;
    if (!keys.includes(key)) keys.push(key);
  });
  const map = {};
  keys.forEach((key, i) => {
    map[key] = BLOCK_COLORS[i % BLOCK_COLORS.length];
  });
  return map;
}

function WeeklyCalendarView({ weekStartDate, blocks, tasks, onPrevWeek, onNextWeek }) {
  // build color map once per render so all blocks on screen are consistent
  const colorMap = buildColorMap(blocks);

  // returns the palette color for a block using its base task name as the key
  const getTaskColor = (block) => {
    const base = block.taskTitle?.replace(/\s*\(Part \d+\/\d+\)$/, "") ?? "";
    const key  = block.taskId != null ? String(block.taskId) : base;
    return colorMap[key] ?? BLOCK_COLORS[0];
  };

  const getWeekDates = () => {
    const dates = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(weekStartDate);
      d.setDate(weekStartDate.getDate() + i);
      dates.push(d);
    }
    return dates;
  };

  const weekDates = getWeekDates();

  const formatWeekHeader = () => {
    const start = weekDates[0];
    const end = weekDates[6];

    const startMonth = start.toLocaleString("default", { month: "long" });
    const endMonth = end.toLocaleString("default", { month: "long" });

    if (start.getMonth() === end.getMonth()) {
      return `${startMonth} ${start.getDate()} - ${end.getDate()}, ${end.getFullYear()}`;
    }

    return `${startMonth} ${start.getDate()} - ${endMonth} ${end.getDate()}, ${end.getFullYear()}`;
  };

  const timeToMinutes = (timeStr) => {
    const [hours, minutes] = timeStr.split(":").map(Number);
    return hours * 60 + minutes;
  };

  const getBlockStyle = (block) => {
    const startMinutes = timeToMinutes(block.startTime);
    const endMinutes = timeToMinutes(block.endTime);

    const top = ((startMinutes - START_HOUR * 60) / 60) * HOUR_HEIGHT;
    const height = ((endMinutes - startMinutes) / 60) * HOUR_HEIGHT;

    return {
      top: `${top}px`,
      height: `${height}px`,
      backgroundColor: getTaskColor(block),
    };
  };

  // returns a color string for the score badge based on urgency
  const getScoreColor = (score) => {
    if (score == null) return "rgba(0,0,0,0.2)";
    if (score >= 75) return "#dc2626";
    if (score >= 50) return "#f59e0b";
    return "#16a34a";
  };

  const getBlockPriorityScore = (block) => {
    if (block.priorityScore != null) return block.priorityScore;
    if (block.priority_score != null) return block.priority_score;
    if (block.score != null) return block.score;

    const baseTitle = block.taskTitle.replace(/\s*\(Part \d+\/\d+\)$/, "");

    const matchedTask =
      tasks?.find((task) => task.id != null && block.taskId != null && task.id === block.taskId) ||
      tasks?.find((task) => task.name === baseTitle);

    return matchedTask?.priorityScore ?? null;
  };

  const timeLabels = [];
  for (let hour = START_HOUR; hour <= END_HOUR; hour++) {
    const suffix = hour >= 12 ? "pm" : "am";
    const hour12 = hour > 12 ? hour - 12 : hour;
    timeLabels.push(`${hour12}${suffix}`);
  }

  const formatDateToYYYYMMDD = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
    };

  // Semester label (approximate based on current month)
  const getSemesterLabel = () => {
    const month = weekStartDate.getMonth() + 1;
    const year = weekStartDate.getFullYear();
    if (month >= 8 && month <= 12) return `Fall ${year}`;
    if (month >= 1 && month <= 5) return `Spring ${year}`;
    return `Summer ${year}`;
  };

  return (
    <div className="weekly-calendar-panel">
      {/* schedule title bar */}
      <div className="schedule-title-bar">
        <span className="schedule-title-text">
          Class Schedule for {getSemesterLabel()}
        </span>
      </div>

      <div className="weekly-panel-header">
        <h2>{formatWeekHeader()}</h2>
        <div className="calendar-nav">
          <button onClick={onPrevWeek}>&#8249;</button>
          <button onClick={onNextWeek}>&#8250;</button>
        </div>
      </div>

      <div className="weekly-calendar-wrapper">
        <div className="weekly-time-column">
          <div className="weekly-time-header-spacer" />
          {timeLabels.map((label) => (
            <div key={label} className="weekly-time-slot">
              {label}
            </div>
          ))}
        </div>

        <div className="weekly-calendar-grid-area">
          <div className="weekly-day-header-row">
            {weekDates.map((date, index) => (
              <div key={index} className="weekly-day-header">
                <div className="weekly-day-name">{DAYS[index]}</div>
                <div className="weekly-day-date">{date.getDate()}</div>
              </div>
            ))}
          </div>

          <div className="weekly-grid-body">
            {weekDates.map((_, dayIndex) => (
              <div key={dayIndex} className="weekly-day-column">
                {Array.from({ length: END_HOUR - START_HOUR + 1 }).map((_, hourIndex) => (
                  <div key={hourIndex} className="weekly-hour-cell" />
                ))}

                {blocks
                  .filter((block) => block.date === formatDateToYYYYMMDD(weekDates[dayIndex]))
                  .map((block, index) => (
                    <div
                      key={`${block.taskId}-${index}`}
                      className="weekly-task-block"
                      style={getBlockStyle(block)}
                      title={`${block.taskTitle} (${block.startTime} - ${block.endTime})`}
                    >
                      <div className="weekly-task-block-inner">
                        <span className="weekly-task-block-title">
                          {block.taskTitle}
                        </span>
                        {/* score badge: color-coded by urgency */}
                        <span
                          className="weekly-task-block-score"
                          style={{ background: getScoreColor(getBlockPriorityScore(block)) }}
                        >
                          {getBlockPriorityScore(block) != null ? Math.round(getBlockPriorityScore(block)) : "–"}
                        </span>
                      </div>

                      {(timeToMinutes(block.endTime) - timeToMinutes(block.startTime)) >= 45 && (
                        <span className="weekly-task-block-time">
                          {block.startTime} – {block.endTime}
                        </span>
                      )}
                    </div>
                  ))}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default WeeklyCalendarView;