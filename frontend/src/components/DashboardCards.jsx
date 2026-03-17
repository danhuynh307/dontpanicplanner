import React from "react";
import { useNavigate } from "react-router-dom";

function DashboardCards() {

  const navigate = useNavigate();

  return (
    <div className="top-cards">

      <div
        className="info-card"
        onClick={() => navigate("/availability")}
        style={{ cursor: "pointer" }}
      >
        <h3>Weekly Availability</h3>
        <p>Set your general weekly schedule and available hours</p>
      </div>

      <div
        className="info-card"
        onClick={() => navigate("/schedule")}
        style={{ cursor: "pointer" }}
      >
        <h3>Weekly Schedule</h3>
        <p>View your scheduled tasks with time blocks</p>
      </div>

    </div>
  );
}

export default DashboardCards;