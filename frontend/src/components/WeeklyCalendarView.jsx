import React from "react";

const DAYS = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
const START_HOUR = 7;
const END_HOUR = 21;
const HOUR_HEIGHT = 64; // px per hour

function WeeklyCalendarView({ weekStartDate, blocks, onPrevWeek, onNextWeek }) {
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
      backgroundColor: block.color || "#8ab4f8",
    };
  };

  const timeLabels = [];
  for (let hour = START_HOUR; hour <= END_HOUR; hour++) {
    const suffix = hour >= 12 ? "pm" : "am";
    const hour12 = hour > 12 ? hour - 12 : hour;
    timeLabels.push(`${hour12}${suffix}`);
  }

  return (
    <div className="weekly-calendar-panel">
      <div className="weekly-panel-header">
        <h2>{formatWeekHeader()}</h2>
        <div className="calendar-nav">
          <button onClick={onPrevWeek}>{"<"}</button>
          <button onClick={onNextWeek}>{">"}</button>
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
                  .filter((block) => block.dayOfWeek === dayIndex)
                  .map((block, index) => (
                    <div
                      key={`${block.taskId}-${index}`}
                      className="weekly-task-block"
                      style={getBlockStyle(block)}
                      title={`${block.taskTitle} (${block.startTime} - ${block.endTime})`}
                    >
                      <span>{block.taskTitle}</span>
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