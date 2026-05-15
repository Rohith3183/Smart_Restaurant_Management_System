import React, { useState } from "react";
import { useParams } from "react-router-dom";
import "../PublicMenuPage.css"; // reuse same gradient + card styling
import { feedbackAPI } from "../services/api";

const FEATURES = [
  { key: "food", label: "Food" },
  { key: "ambiance", label: "Ambiance" },
  { key: "ingredients", label: "Quality of Ingredients" },
  { key: "service", label: "Service" },
  { key: "cleanliness", label: "Cleanliness" },
  { key: "valueForMoney", label: "Value for Money" },
  { key: "overall", label: "Overall Experience" },
];

const PublicFeedbackPage = () => {
  const { code } = useParams();
  const [ratings, setRatings] = useState({
    food: 0,
    ambiance: 0,
    ingredients: 0,
    service: 0,
    cleanliness: 0,
    valueForMoney: 0,
    overall: 0,
  });
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleStarClick = (featureKey, value) => {
    setRatings((prev) => ({
      ...prev,
      [featureKey]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");

    const anyZero = Object.values(ratings).some((v) => v === 0);
    if (anyZero) {
      setError("Please rate all items before submitting.");
      return;
    }

    setSubmitting(true);
    try {
      await feedbackAPI.submitPublic(code, {
        food: ratings.food,
        ambiance: ratings.ambiance,
        ingredients: ratings.ingredients,
        service: ratings.service,
        cleanliness: ratings.cleanliness,
        valueForMoney: ratings.valueForMoney,
        overall: ratings.overall,
      });
      setMessage("Thank you! Your feedback has been submitted.");
      setRatings({
        food: 0,
        ambiance: 0,
        ingredients: 0,
        service: 0,
        cleanliness: 0,
        valueForMoney: 0,
        overall: 0,
      });
    } catch (err) {
      setError(
        err?.response?.data?.message ||
          "Failed to submit feedback. Please try again."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="pm-page pm-page-bg">
      <div className="pm-main" style={{ maxWidth: "720px" }}>
        <div className="pm-card single-card">
          <div className="pm-card-top-strip" />
          <div className="pm-card-inner">
            <div className="pm-card-header">
              <h2 className="pm-card-title">Customer Feedback</h2>
             
            </div>
            <div className="pm-card-divider" />

            {error && <p className="pf-error-text">{error}</p>}
            {message && <p className="pf-success-text">{message}</p>}

            <form onSubmit={handleSubmit}>
              <div className="pf-grid">
                {FEATURES.map((f) => (
                  <div key={f.key} className="pf-row">
                    <div className="pf-label">{f.label}</div>
                    <div className="pf-stars">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <span
                          key={star}
                          className={
                            star <= ratings[f.key]
                              ? "pf-star pf-star-active"
                              : "pf-star"
                          }
                          onClick={() => handleStarClick(f.key, star)}
                        >
                          ★
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>

              <div className="pf-submit-wrapper">
                <button
                  type="submit"
                  className="pf-submit-btn"
                  disabled={submitting}
                >
                  {submitting ? "Submitting..." : "Submit Feedback"}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PublicFeedbackPage;
