@echo off
echo =========================================
echo   ProBuilder - Generate Release Bundle
echo =========================================
echo.
echo Cleaning previous build...
call gradlew.bat clean
echo.
echo Building release AAB...
call gradlew.bat :app:bundleRelease --no-daemon
echo.
if exist "app\build\outputs\bundle\release\app-release.aab" (
    echo =========================================
    echo   SUCCESS! AAB file is ready at:
    echo   app\build\outputs\bundle\release\app-release.aab
    echo =========================================
    explorer "app\build\outputs\bundle\release"
) else (
    echo =========================================
    echo   Build may have failed. Check output above.
    echo =========================================
)
pause
