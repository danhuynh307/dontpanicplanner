import React from "react";
import { useNavigate } from "react-router-dom";

function DashboardCards() {

  const navigate = useNavigate();
    // cards at top that are clickable, redirect to pages
  return (
    <div className="top-cards">

      <div className="info-card" onClick={() => navigate("/availability")}>
        <div className="info-card-icon">&#128197;</div>
        <div className="info-card-text">
          <h3>Weekly Availability</h3>
          <p>Set your general weekly schedule and available hours</p>
        </div>
        <span className="info-card-arrow">&#8250;</span>
      </div>

      <div className="info-card" onClick={() => navigate("/schedule")}>
        <div className="info-card-icon">&#128203;</div>
        <div className="info-card-text">
          <h3>Weekly Schedule</h3>
          <p>View your scheduled tasks with time blocks</p>
        </div>
        <span className="info-card-arrow">&#8250;</span>
      </div>

    </div>
  );
}

export default DashboardCards;