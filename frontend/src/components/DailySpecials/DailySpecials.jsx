import { useState, useEffect } from 'react';
import { fetchDailySpecials } from '../../services/api.js';

const AREA_ET = {
  American: 'Ameerika', British: 'Briti', Canadian: 'Kanada', Chinese: 'Hiina',
  Croatian: 'Horvaatia', Dutch: 'Hollandi', Egyptian: 'Egiptuse', Filipino: 'Filipiini',
  French: 'Prantsuse', Greek: 'Kreeka', Indian: 'India', Irish: 'Iiri',
  Italian: 'Itaalia', Jamaican: 'Jamaica', Japanese: 'Jaapani', Kenyan: 'Keenia',
  Malaysian: 'Malaisia', Mexican: 'Mehhiko', Moroccan: 'Maroko', Polish: 'Poola',
  Portuguese: 'Portugali', Russian: 'Vene', Spanish: 'Hispaania', Thai: 'Tai',
  Tunisian: 'Tuneesia', Turkish: 'Türgi', Vietnamese: 'Vietnami', Unknown: 'Muu',
};

const CATEGORY_ET = {
  Beef: 'Veiseliha', Breakfast: 'Hommikusöök', Chicken: 'Kana', Dessert: 'Magustoit',
  Goat: 'Kitseliiga', Lamb: 'Lambaliha', Miscellaneous: 'Muu', Pasta: 'Pasta',
  Pork: 'Sealiha', Seafood: 'Mereannid', Side: 'Lisand', Starter: 'Eelroog',
  Vegan: 'Taimetoit', Vegetarian: 'Taimetoit',
};

export default function DailySpecials() {
  const [meals, setMeals] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDailySpecials()
      .then(setMeals)
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="daily-specials">
        <h4>
          <span className="material-symbols-outlined">restaurant</span>
          Päevapraad
        </h4>
        <div className="daily-specials-loading">Laen menüüd...</div>
      </div>
    );
  }

  if (meals.length === 0) return null;

  return (
    <div className="daily-specials">
      <h4>
        <span className="material-symbols-outlined">restaurant</span>
        Päevapraad
      </h4>
      <div className="daily-specials-list">
        {meals.map((meal, i) => (
          <div key={i} className="meal-card">
            <img src={meal.imageUrl} alt={meal.name} className="meal-card-img" />
            <div className="meal-card-body">
              <p className="meal-card-name">{meal.name}</p>
              <p className="meal-card-meta">
                {AREA_ET[meal.area] || meal.area} · {CATEGORY_ET[meal.category] || meal.category}
              </p>
            </div>
          </div>
        ))}
      </div>
      <p className="daily-specials-source">
        Andmed: <a href="https://www.themealdb.com" target="_blank" rel="noopener noreferrer">TheMealDB</a>
      </p>
    </div>
  );
}
