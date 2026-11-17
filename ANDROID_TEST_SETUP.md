# Android Test — Login → Home → Detail

Demo account: `demo` / `123456` (only for classroom practice; not real authentication).

## Navigation
- `LoginActivity`: launcher, validates demo credentials
- `HomeActivity`: simple ListView + logout
- `DetailActivity`: custom ProductAdapter, ImageView, title, origin, description, price
- Fruit catalog currently uses static `DataSource`, **not** SQLite

## Run
Open the repository in Android Studio, sync Gradle, choose an emulator, and Run `app`.

Every `fruit_*.xml` drawable is a lightweight bundled illustration so the project builds without missing assets. To use the higher-resolution PNG pack supplied in chat, remove each matching `fruit_*.xml` file and replace it with `fruit_*.png` in `app/src/main/res/drawable/`. Do not leave both resources with the same basename.

This is a demo exercise, not a production login system.
