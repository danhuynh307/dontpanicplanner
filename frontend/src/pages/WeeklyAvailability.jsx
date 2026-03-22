import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/WeeklyAvailability.css";
import {
  getAvailability,
  saveAvailability,
} from "../services/availabilityService";

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

const startHour = 6;
const endHour = 24;
const slotsPerHour = 4;
const totalSlots = (endHour - startHour) * slotsPerHour;

function createInitialGrid() {
  return Array.from({ length: days.length }, () =>
    Array(totalSlots).fill(false)
  );
}

function formatHourLabel(hour) {
  const suffix = hour >= 12 ? "PM" : "AM";
  const displayHour = hour % 12 === 0 ? 12 : hour % 12;
  return `${displayHour} ${suffix}`;
}

function slotToTime(slotIndex) {
  const totalMinutes = startHour * 60 + slotIndex * 15;
  const hour = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  return `${String(hour).padStart(2, "0")}:${String(minutes).padStart(
    2,
    "0"
  )}`;
}

function timeToSlot(timeString) {
  const [hour, minute] = timeString.split(":").map(Number);
  const totalMinutes = hour * 60 + minute;
  return (totalMinutes - startHour * 60) / 15;
}

function convertGridToBlocks(grid) {
  const blocks = [];

  for (let dayIndex = 0; dayIndex < grid.length; dayIndex++) {
    let startSlot = null;

    for (let slotIndex = 0; slotIndex <= grid[dayIndex].length; slotIndex++) {
      const isSelected =
        slotIndex < grid[dayIndex].length ? grid[dayIndex][slotIndex] : false;

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

function WeeklyAvailability() {
  const navigate = useNavigate();

  const [grid, setGrid] = useState(createInitialGrid());
  const [isDragging, setIsDragging] = useState(false);
  const [dragMode, setDragMode] = useState("add");
  const [saveMessage, setSaveMessage] = useState("");

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

  return (
    <div
      className="availability-page"
      onMouseUp={handleMouseUp}
      onMouseLeave={handleMouseUp}
    >
      <div className="availability-card">
        <div className="availability-header">
          <button className="back-button" onClick={() => navigate("/")}>
            ←
          </button>

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