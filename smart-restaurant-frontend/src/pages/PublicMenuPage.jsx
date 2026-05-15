// src/pages/PublicMenuPage.jsx
import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import "../PublicMenuPage.css"; // import plain CSS

const PublicMenuPage = () => {
  const { code } = useParams();
  const [menu, setMenu] = useState([]);
  const [restaurant, setRestaurant] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        const res = await fetch(
          `http://localhost:9095/api/v1/public/menu/${code}`
        );
        if (!res.ok) throw new Error("Failed to load menu");
        const data = await res.json();

        setRestaurant({
          name: data.restaurantName,
          contact: data.contact,
          location: data.location
        });

        setMenu(data.items || []);
      } catch (e) {
        setError(e.message || "Failed to load menu");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [code]);

  const categories = [...new Set(menu.map((i) => i.categoryName))];
  const itemsByCategory = (cat) =>
    menu.filter((i) => i.categoryName === cat && i.available);

  if (loading) {
    return (
      <div className="pm-page pm-page-bg">
        <div className="pm-loader" />
      </div>
    );
  }

  if (error || !restaurant) {
    return (
      <div className="pm-page pm-page-bg">
        <div className="pm-error-card">
          <h2>Menu not available</h2>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="pm-page pm-page-bg">
      <header className="pm-header">
        <div className="pm-header-left">
          <p className="pm-header-tagline">LIVE MENU</p>
          <h1 className="pm-header-title">{restaurant.name}</h1>
          {restaurant.location && (
            <p className="pm-header-location">{restaurant.location}</p>
          )}
        </div>
        <div className="pm-header-right">
          {restaurant.contact && (
            <p className="pm-header-contact">Contact: +91 {restaurant.contact}</p>
          )}
          <p className="pm-header-items">
            {menu.filter((m) => m.available).length} Items Available
          </p>
        </div>
      </header>

      <main className="pm-main">
        {categories.length === 0 ? (
          <p className="pm-empty">Menu will be available soon.</p>
        ) : (
          <div className="pm-card-grid">
            {categories.map((category) => {
              const items = itemsByCategory(category);
              if (items.length === 0) return null;

              return (
                <div key={category} className="pm-card">
                  <div className="pm-card-top-strip" />

                  <div className="pm-card-inner">
                    {/* Card header */}
                    <div className="pm-card-header">
                      <h2 className="pm-card-title">{category}</h2>
                      <span className="pm-card-badge">
                        {items.length} item{items.length > 1 ? "s" : ""}
                      </span>
                    </div>

                    <div className="pm-card-divider" />

                    {/* Items */}
                    <div className="pm-item-list">
                      {items.map((item, idx) => (
                        <div key={item.id} className="pm-item-row">
                          <div className="pm-item-left">
                            <span className="pm-item-index">
                              {String(idx + 1).padStart(2, "0")}
                            </span>
                            <span className="pm-item-name">{item.name}</span>
                          </div>
                          <div className="pm-item-right">
                            <span className="pm-item-price">
                              ₹{parseFloat(item.price || 0).toFixed(2)}
                            </span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>
    </div>
  );
};

export default PublicMenuPage;
