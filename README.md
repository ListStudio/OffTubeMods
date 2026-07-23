# OffTubeMods — Notification Mods

Этот репозиторий содержит два простых мода/overlay'а для Android Jetpack Compose, которые демонстрируют:

- Винтажный Winamp-оверлей (SVG asset + Composable)
- Кастомизацию «OxygenOS» и небольшой "dash" (Composable Quick Dash)

Файлы были добавлены для быстрого старта — соберите проект в Android Studio и добавьте зависимости (Coil, Material3, lifecycle) в build.gradle модуля.

Raw URL для SVG-оверлея:

https://raw.githubusercontent.com/ListStudio/OffTubeMods/main/assets/winamp_bug.svg

Инструкции:
1. Склонируйте репозиторий.
2. Добавьте зависимости в модуль: Coil (io.coil-kt:coil-compose), Material3, lifecycle-runtime-compose.
3. Импортируйте Composable'ы из пакета com.liststudio.offtubemods.mods в ваш проект и используйте их вместе с NotificationBar.
