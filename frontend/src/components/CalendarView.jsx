import React, { useMemo, useState } from "react";

function CalendarView({ selectedDate, setSelectedDate }) {
  const today = new Date();

  // start from the Sunday of the current week
  const getStartOfWeek = (date) => {
    const d = new Date(date);
    const day = d.getDay(); // sunday default
    d.setDate(d.getDate() - day); // get current date
    d.setHours(0, 0, 0, 0); // reset time
    return d;
  };

  const [calendarStart, setCalendarStart] = useState(getStartOfWeek(today)); // starting day

  // 4 week view (28 days)
  const days = useMemo(() => {
    return Array.from({ length: 28 }, (_, index) => {
      const date = new Date(calendarStart);
      date.setDate(calendarStart.getDate() + index);
      return date;
    });
  }, [calendarStart]);

  // check two days are the same
  const sameDay = (d1, d2) =>
    d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate();

  // check if day is in past to gray out
  const isPastDay = (date) => {
    const compare = new Date(date);
    compare.setHours(0, 0, 0, 0);

    const now = new Date(today);
    now.setHours(0, 0, 0, 0);

    return compare < now;
  };

  //format header for calendar
  const formatHeader = () => {
    const first = days[0];
    const last = days[27];

    const firstMonth = first.toLocaleString("default", { month: "long" });
    const lastMonth = last.toLocaleString("default", { month: "long" });

    if (first.getMonth() === last.getMonth()) {
      // same month then display only that month
      return `${firstMonth} ${first.getDate()} - ${last.getDate()}, ${last.getFullYear()}`;
    }

    // display both months if span multiple
    return `${firstMonth} ${first.getDate()} - ${lastMonth} ${last.getDate()}, ${last.getFullYear()}`;
  };

  // logic for left arrow (going back 4 weeks)
  const handlePrev = () => {
    const newStart = new Date(calendarStart);
    newStart.setDate(calendarStart.getDate() - 28);
    setCalendarStart(newStart);
  };

  // logic for right arrow (going forward 4 weeks)
  const handleNext = () => {
    const newStart = new Date(calendarStart);
    newStart.setDate(calendarStart.getDate() + 28);
    setCalendarStart(newStart);
  };

  // calendar and buttons
  return (
    <div className="calendar-panel">
      <div className="calendar-header">
        <h2>{formatHeader()}</h2>
        <div className="calendar-nav">
          <button onClick={handlePrev}>{"<"}</button>
          <button onClick={handleNext}>{">"}</button>
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

          return (
            <div
              key={index}
              className={`calendar-cell
                ${isSelected ? "selected" : ""}
                ${isToday ? "today" : ""}
                ${isPast ? "past-day" : ""}`
              }
              onClick={() => setSelectedDate(dateObj)}
            >
              <span className="date-number">{dateObj.getDate()}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default CalendarView;