CREATE TABLE IF NOT EXISTS restaurants (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           name TEXT NOT NULL,
                                           category TEXT,
                                           location TEXT,
                                           likes INTEGER DEFAULT 0,
                                           dislikes INTEGER DEFAULT 0,
                                           net_score INTEGER DEFAULT 0,
                                           weekly_likes INTEGER DEFAULT 0,
                                           logo TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS menu_items (
                                          id INTEGER PRIMARY KEY AUTOINCREMENT,
                                          restaurant_id INTEGER NOT NULL,
                                          item_name TEXT NOT NULL,
                                          price REAL NOT NULL,
                                          FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username TEXT UNIQUE NOT NULL,
                                     password TEXT NOT NULL,
                                     email TEXT,
                                     first_name TEXT,
                                     last_name TEXT,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_history (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                                            user_id INTEGER NOT NULL,
                                            restaurant_id INTEGER NOT NULL,
                                            liked INTEGER,
                                            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            UNIQUE (user_id, restaurant_id),
                                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                            FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);
