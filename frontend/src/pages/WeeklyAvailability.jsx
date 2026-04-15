import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/WeeklyAvailability.css";
import {
  getAvailability,
  saveAvailability,
} from "../services/availabilityService";
// days of week
const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const backendDayNames = [
  "SUNDAY",
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
];
// availability window defaults
const startHour = 6;
const endHour = 24;
const slotsPerHour = 4;
const totalSlots = (endHour - startHour) * slotsPerHour;

// creates empty grid
function createInitialGrid() {
  return Array.from({ length: days.length }, () =>
    Array(totalSlots).fill(false)
  );
}
// 24h time to am + pm
function formatHourLabel(hour) {
  const suffix = hour >= 12 ? "PM" : "AM";
  const displayHour = hour % 12 === 0 ? 12 : hour % 12;
  return `${displayHour} ${suffix}`;
}
// computes the time for each block
function slotToTime(slotIndex) {
  const totalMinutes = startHour * 60 + slotIndex * 15;
  const hour = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  return `${String(hour).padStart(2, "0")}:${String(minutes).padStart(
    2,
    "0"
  )}`;
}
// time string to block
function timeToSlot(timeString) {
  const [hour, minute] = timeString.split(":").map(Number);
  const totalMinutes = hour * 60 + minute;
  return (totalMinutes - startHour * 60) / 15;
}
// converts grid to back end blocks and merges consecutive slots
function convertGridToBlocks(grid) {
  const blocks = [];
  for (let dayIndex = 0; dayIndex < grid.length; dayIndex++) {
    let startSlot = null;

    for (let slotIndex = 0; slotIndex <= grid[dayIndex].length; slotIndex++) {
      const isSelected = slotIndex < grid[dayIndex].length ? grid[dayIndex][slotIndex] : false;

      if (isSelected && startSlot === null) {
        startSlot = slotIndex;
      }
      if (!isSelected && startSlot !== null) {
        blocks.push({
          dayOfWeek: backendDayNames[dayIndex],
          startTime: slotToTime(startSlot),
          endTime: slotToTime(slotIndex),
        });

        startSlot = null;
      }
    }
  }

  return blocks;
}
// backend blocks back to grid
function convertBlocksToGrid(blocks) {
  const newGrid = createInitialGrid();

  const dayMap = {
    SUNDAY: 0,
    MONDAY: 1,
    TUESDAY: 2,
    WEDNESDAY: 3,
    THURSDAY: 4,
    FRIDAY: 5,
    SATURDAY: 6,
  };

  blocks.forEach((block) => {
    const dayIndex = dayMap[block.dayOfWeek];
    if (dayIndex === undefined) return;

    const startSlot = timeToSlot(block.startTime);
    const endSlot = timeToSlot(block.endTime);

    for (let i = startSlot; i < endSlot; i++) {
      if (i >= 0 && i < totalSlots) {
        newGrid[dayIndex][i] = true;
      }
    }
  });

  return newGrid;
}

// parses "8", "8:30", "8am", "8:30pm" etc into { hour, minute }
function parseTime(str) {
  str = str.trim().toLowerCase();
  const isPM = str.endsWith("pm");
  const isAM = str.endsWith("am");
  str = str.replace(/am|pm/, "").trim();
  let hour, minute = 0;
  if (str.includes(":")) {
    [hour, minute] = str.split(":").map(Number);
  } else {
    hour = Number(str);
  }
  if (isPM && hour !== 12) hour += 12;
  if (isAM && hour === 12) hour = 0;
  return { hour, minute };
}

// all user actions (drag, click, save button)
function WeeklyAvailability() {
  const navigate = useNavigate();
  const [grid, setGrid] = useState(createInitialGrid());
  const [isDragging, setIsDragging] = useState(false);
  const [dragMode, setDragMode] = useState("add");
  const [saveMessage, setSaveMessage] = useState("");
  const [quickInputs, setQuickInputs] = useState(Array(days.length).fill(""));

  useEffect(() => {
    loadAvailability();
  }, []);

  const loadAvailability = async () => {
    try {
      const blocks = await getAvailability();
      setGrid(convertBlocksToGrid(blocks));
    } catch (error) {
      console.error("Failed to load availability:", error);
    }
  };

  const handleSave = async () => {
    try {
      const blocks = convertGridToBlocks(grid);
      await saveAvailability(blocks);
      setSaveMessage("Availability saved");
    } catch (error) {
      console.error("Failed to save availability:", error);
      setSaveMessage("Failed to save availability");
    }
  };

  const updateCell = (dayIndex, slotIndex, value) => {
    setGrid((prev) => {
      const updated = prev.map((day) => [...day]);
      updated[dayIndex][slotIndex] = value;
      return updated;
    });
  };

  const handleMouseDown = (dayIndex, slotIndex) => {
    const isSelected = grid[dayIndex][slotIndex];
    const mode = isSelected ? "remove" : "add";

    setIsDragging(true);
    setDragMode(mode);
    updateCell(dayIndex, slotIndex, mode === "add");
  };

  const handleMouseEnter = (dayIndex, slotIndex) => {
    if (!isDragging) return;
    updateCell(dayIndex, slotIndex, dragMode === "add");
  };

  const handleMouseUp = () => {
    setIsDragging(false);
  };

  const clearAll = () => {
    setGrid(createInitialGrid());
    setSaveMessage("");
  };

  // applies a "start-end" range string to a single day column in the grid
  const applyQuickInput = (dayIndex, value) => {
    if (!value.includes("-")) return;
    const dashIndex = value.lastIndexOf("-");
    const startStr = value.slice(0, dashIndex);
    const endStr = value.slice(dashIndex + 1);
    try {
      const start = parseTime(startStr);
      const end   = parseTime(endStr);
      const startSlot = timeToSlot(`${String(start.hour).padStart(2,"0")}:${String(start.minute).padStart(2,"0")}`);
      const endSlot   = timeToSlot(`${String(end.hour).padStart(2,"0")}:${String(end.minute).padStart(2,"0")}`);
      if (isNaN(startSlot) || isNaN(endSlot) || startSlot >= endSlot) return;
      const clampedStart = Math.max(0, startSlot);
      const clampedEnd   = Math.min(totalSlots, endSlot);
      setGrid((prev) => {
        const updated = prev.map((day) => [...day]);
        for (let i = clampedStart; i < clampedEnd; i++) {
          updated[dayIndex][i] = true;
        }
        return updated;
      });
    } catch (e) {
      // invalid input — silently ignore
    }
  };

  const handleQuickInputChange = (dayIndex, value) => {
    setQuickInputs((prev) => {
      const next = [...prev];
      next[dayIndex] = value;
      return next;
    });
  };

  // triggered on Enter key inside a quick-set input
  const handleQuickInputKey = (e, dayIndex) => {
    if (e.key === "Enter") applyQuickInput(dayIndex, quickInputs[dayIndex]);
  };

  return (
    <div
      className="availability-wrapper"
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
    >
      {/* top bar */}
      <div className="avail-top-bar">
        <button className="avail-back-btn" onClick={() => navigate("/")}>
          &#8592; Back
        </button>
        <span className="avail-top-bar-title">
          <span className="avail-top-bar-brand">Don't Panic Planner</span>
          <span className="avail-top-bar-sep">|</span>
          Weekly Availability
        </span>
        <div style={{ width: 80 }} />
      </div>

      <div className="availability-card">
        <div className="availability-header">
          <div>
            <h1>Weekly Availability</h1>
            <p>Set your general weekly schedule by clicking and dragging</p>
          </div>
        </div>

        <div className="availability-actions">
          <button className="clear-button" onClick={clearAll}>
            Clear All
          </button>
          <button className="save-button" onClick={handleSave}>
            Save Availability
          </button>
        </div>

        {saveMessage && <p className="save-message">{saveMessage}</p>}

        <div className="availability-grid">
          <div className="grid-corner"></div>

          {days.map((day) => (
            <div key={day} className="day-header">
              {day}
            </div>
          ))}

          {/* quick-set row lives inside the grid so columns align perfectly */}
          <div className="quick-set-label">Quick Set</div>
          {days.map((day, dayIndex) => (
            <div key={`qs-${day}`} className="quick-set-cell">
              <input
                className="quick-set-input"
                placeholder="8:00-17:00"
                value={quickInputs[dayIndex]}
                onChange={(e) => handleQuickInputChange(dayIndex, e.target.value)}
                onKeyDown={(e) => handleQuickInputKey(e, dayIndex)}
              />
              <button
                className="quick-set-btn"
                onClick={() => applyQuickInput(dayIndex, quickInputs[dayIndex])}
              >
                Set
              </button>
            </div>
          ))}

          {Array.from({ length: totalSlots }).map((_, slotIndex) => {
            const hour = startHour + Math.floor(slotIndex / 4);
            const showLabel = slotIndex % 4 === 0;

            return (
              <React.Fragment key={slotIndex}>
                <div className="time-label">
                  {showLabel ? formatHourLabel(hour) : ""}
                </div>

                {days.map((_, dayIndex) => (
                  <div
                    key={`${dayIndex}-${slotIndex}`}
                    className={`time-cell ${
                      grid[dayIndex][slotIndex] ? "selected" : ""
                    }`}
                    onMouseDown={() => handleMouseDown(dayIndex, slotIndex)}
                    onMouseEnter={() => handleMouseEnter(dayIndex, slotIndex)}
                  />
                ))}
              </React.Fragment>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export default WeeklyAvailability;