IMPORTANT INSTRUCTIONS FOR DEPLOYMENT

The Client Portal refactoring requires new backend scripts to handle secure, session-based data fetching.

1. Locate the folder: C:\Users\hp\AndroidStudioProjects\ProBuilder\backend_scripts
2. Copy ALL .php files from this folder (`client_project.php`, `client_daily_progress.php`, `client_quotations.php`, `set_project_session.php`, `get_client_projects.php`)
3. Paste them into your server's API directory (the same place where `login.php` and `db.php` reside).
   - If you have a specific `client` folder, put the `client_*.php` files there, but ensure `require '../db.php';` path is correct.
   - Based on my code, I assumed they are in the same directory as `login.php` or a subdirectory.
   - My scripts use `require '../db.php';`, implying they are intended for a subdirectory (e.g., `htdocs/api/client/`).
   - IF you place them in the root API folder (where `db.php` is), you MUST CHANGE line 2 to: `require 'db.php';` in all the new files.

   **Recommendation**:
   Create a folder `client` inside your API root.
   Put `client_project.php`, `client_daily_progress.php`, `client_quotations.php` INSIDE `client/`.
   Put `set_project_session.php` and `get_client_projects.php` in the ROOT API folder (or update paths in Android `Constants.java` if you move them).

   **Current Android `Constants.BASE_URL` assumption**:
   The app appends paths like `client_project.php` directly to `BASE_URL`.
   If `BASE_URL` is `http://.../api/`, then `http://.../api/client_project.php`.
   
   **CRITICAL FIX**:
   My scripts currently have `require '../db.php';` which assumes they are in a subdirectory.
   BUT the Android app calls `Constants.BASE_URL + "client_project.php"`.
   This means the file is expected at the ROOT of `BASE_URL`.
   So the `require` should be `require 'db.php';`.
   
   **I will update the PHP scripts to use `require 'db.php';` now to avoid confusion, assuming they sit next to login.php.**

   [ACTION TAKEN]: I (Antigravity) will auto-correct this in the next step.

4. Verify your `daily_progress` table has the columns: `progress_date`, `description`, `progress_percent`.
5. Verify your `quotations` table has: `title`, `amount`, `status`, `created_at`.

Once these files are in place, the Android app will work securely.
