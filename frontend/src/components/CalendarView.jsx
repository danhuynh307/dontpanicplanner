import React, { useMemo, useState } from "react";

function CalendarView({ selectedDate, setSelectedDate, tasks }) {
  const today = new Date();

  const getCalendarStart = (date) => {
    const d = new Date(date);
    d.setDate(d.getDate() - 7);
    d.setHours(0, 0, 0, 0);
    return d;
  };

  const [calendarStart, setCalendarStart] = useState(getCalendarStart(today));

  const formatDateToYYYYMMDD = (date) => {
    if (!date) return "";
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  };

  const days = useMemo(() => {
    return Array.from({ length: 28 }, (_, index) => {
      const date = new Date(calendarStart);
      date.setDate(calendarStart.getDate() + index);
      return date;
    });
  }, [calendarStart]);

  const sameDay = (d1, d2) =>
    d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate();

  const isPastDay = (date) => {
    const compare = new Date(date);
    compare.setHours(0, 0, 0, 0);

    const now = new Date(today);
    now.setHours(0, 0, 0, 0);

    return compare < now;
  };

  const formatHeader = () => {
    const first = days[0];
    const last = days[27];

    const firstMonth = first.toLocaleString("default", { month: "long" });
    const lastMonth = last.toLocaleString("default", { month: "long" });

    if (first.getMonth() === last.getMonth()) {
      return `${firstMonth} ${first.getDate()} - ${last.getDate()}, ${last.getFullYear()}`;
    }

    return `${firstMonth} ${first.getDate()} - ${lastMonth} ${last.getDate()}, ${last.getFullYear()}`;
  };

  const handlePrev = () => {
    const newStart = new Date(calendarStart);
    newStart.setDate(calendarStart.getDate() - 28);
    setCalendarStart(newStart);
  };

  const handleNext = () => {
    const newStart = new Date(calendarStart);
    newStart.setDate(calendarStart.getDate() + 28);
    setCalendarStart(newStart);
  };

  return (
    <div className="calendar-panel">

      <div className="calendar-header">
        <h2>{formatHeader()}</h2>
        <div className="calendar-legend">
            <div className="legend-item">
              <span className="legend-box current-day-box"></span>
              <span>Current day</span>
            </div>
            <div className="legend-item">
              <span className="legend-box selected-day-box"></span>
              <span>Selected day</span>
            </div>
          </div>

        <div className="calendar-nav">
          <button onClick={handlePrev}>&#8249;</button>
          <button onClick={handleNext}>&#8250;</button>
        </div>
      </div>

      <div className="calendar-day-names">
        {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((day) => (
          <div key={day} className="day-name">
            {day}
          </div>
        ))}
      </div>

      <div className="calendar-grid">
        {days.map((dateObj, index) => {
          const isToday = sameDay(dateObj, today);
          const isPast = isPastDay(dateObj);
          const isSelected = selectedDate && sameDay(dateObj, selectedDate);

          const dateString = formatDateToYYYYMMDD(dateObj);
          const taskCount = tasks.filter((task) => task.dueDate === dateString).length;

          return (
            <div
              key={index}
              className={`calendar-cell
                ${isSelected ? "selected" : ""}
                ${isToday ? "today" : ""}
                ${isPast ? "past-day" : ""}`}
              onClick={() => setSelectedDate(dateObj)}
            >
              <span className="date-number">{dateObj.getDate()}</span>

              {taskCount > 0 && (
                <div className="task-count-badge">
                  {taskCount}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default CalendarView;