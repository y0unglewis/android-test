# Fruit Catalog Demo — UI and navigation

## Screens
- Login: start screen, username + password, link to Register
- Register: form validates username, password, confirmation, returns to Login
- Home: existing simple ListView, detail button and bottom navigation
- Detail: custom ProductAdapter product cards, 15 ImageView fruit drawings, back button and bottom navigation

**Dashboard deliberately not added.** The bottom navigation contains Home and Products only.

## Demo credentials
- Username: `demo`
- Password: `123456`

Register can additionally create **one local demo account per installation**. DemoAuth uses SharedPreferences with a salted SHA-256 value for teaching only; it is not a production authentication service, password manager, or server. The built-in demo account remains available.

## Navigation
Login → Home → Detail; Register → Login. The Detail header back button and the device Back action both return to the previous Home. Logout clears the Home task and opens Login.

When tapping a fruit name in Home, Detail opens and scrolls to that fruit. Tapping the Products bottom tab opens the full product list.

## Running
`git fetch origin`, check out the feature branch, Gradle Sync and Run `app` in Android Studio. Only Java, XML and existing Material Components are used. No new library is required.

The existing DataSource still contains static example items, not a SQLite database. The `fruit_*.xml` VectorDrawables remain the ImageView assets.
