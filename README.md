# Smart Emergency Response System

A campus emergency-response prototype with a React dashboard, Spring Boot API, persistent H2 database, and a separate FastAPI crowd-risk estimator.

## What it provides

- Sign-in and role-aware screens for students, faculty, campus guards, security verifiers, and administrators.
- Incident reporting with a floor selector and an individual room-selectable map for each of the six campus floors.
- Security review before any reported incident becomes a campus-wide alert.
- Role-specific instructions for students, faculty, and guards after verification.
- A guard resolution request followed by a separate security decision to clear or retain the alert.
- In-app incident updates every five seconds, visible status history, and floor-plan alert markers.
- A separate, explicitly rule-based crowd-risk estimate from a user-supplied headcount.

Alerts are in-app only. Email, SMS, push notifications, dispatch integration, and live occupancy sensors are not configured.

## Requirements

- Java 25 or newer and Maven
- Node.js and npm
- Python 3.10+ for the AI service

## Project layout

```text
backend/       Spring Boot API, source, tests, and Maven build
frontend/      React/Vite dashboard and static assets
ai-service/    FastAPI crowd-risk service and optional training example
database/      SQL schema and sample data references
docs/          API documentation, requirements, and reference assets
render.yaml    Render deployment blueprint
```

Generated environments and build output are excluded by the root `.gitignore` and can be recreated using the commands below. The local H2 database files in `backend/` contain application data; keep or back them up if you need to preserve local accounts and incidents.
The original floor-plan reference is in `docs/assets/`; the dashboard uses the six individual maps in `frontend/public/floors/`. Users select a floor in the map section before choosing a room.

## Run locally

Run each service in its own terminal from the workspace root.

### 1. Backend API

```powershell
Set-Location .\backend
mvn spring-boot:run
```

The backend listens on `http://localhost:8080` and stores local data in `backend/emergency_system.mv.db` using H2. Do not start a second backend against the same database file. For database-lock recovery, stop the old process first and back up the `.mv.db`, `.trace.db`, and `.lock.db` files before moving a stale lock aside.

### 2. Frontend

```powershell
Set-Location .\frontend
npm install
npm run dev
```

Open the Vite URL (normally `http://localhost:5173`). The browser talks to the backend at `http://localhost:8080` and the crowd-risk service at `http://localhost:8000`.

On a new database, create the first administrator from the sign-in page. The setup endpoint is disabled after the first account is saved. The administrator can create faculty, guard, security, and other administrator accounts. Students and faculty can self-register; privileged roles cannot.

To review incidents, sign in with a provisioned `SECURITY` account or an `ADMIN` account using the matching role in the sign-in form. Security and administrators can verify reports, mark pending reports or active alerts as false alarms, close active incidents as resolved with a required note, and review guard resolution requests. False alarms and resolved incidents are removed from active alerts but retained in incident history.

#### Recover administrator access while preserving data

If existing administrator credentials are unavailable, stop the running backend first. From an interactive PowerShell terminal, run the recovery task below. It creates an additional administrator account in the existing database, does not delete or change existing accounts or incidents, and prompts for a new password without echoing it. Use an email address not already assigned to an account and a password of 12–72 characters.

```powershell
Set-Location .\backend
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Dapp.admin-recovery.enabled=true -Dspring.main.web-application-type=none"
```

The recovery runner is disabled during normal app startup and is only enabled by the explicit local command above. Once it reports success, start the backend normally, sign in with the new account as **Administrator**, and create a **Security verifier** account in **Manage accounts**. Never use a shared or hard-coded default password.

### 3. Crowd-risk service

```powershell
Set-Location .\ai-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
python -m uvicorn app:app --reload --port 8000
```

The service currently uses a documented threshold-based baseline, not a trained or validated production model. Its estimate uses only the headcount entered by the user; it does not read occupancy sensors or affect incident status and response decisions.
The `training/` script and `data/` CSV are optional examples and are not used by the running API. The API dependencies are listed in `ai-service/requirements.txt`; the optional training dependencies are in `ai-service/training/requirements.txt`.

## Deploy publicly on Render

The root `render.yaml` defines a React static site, Spring Boot API, and crowd-risk API. Push this project to a Git repository, then create a **Blueprint** in Render and connect that repository. Render will build the services and configure their public URLs. The backend uses a persistent disk for H2; this requires a paid Render web-service plan. Keep the disk attached to the backend service.

After deployment, open the frontend URL and create the first administrator from the initial setup screen. This creates a new, empty hosted database; local accounts and incidents are not copied automatically. Use HTTPS URLs and unique credentials. Do not put real emergency, health, or personal data into this demonstration system; it is not a validated life-safety service.

## Emergency response lifecycle

1. A signed-in user submits an incident. It is saved as `REPORTED` and appears in the security verification queue and campus incident feed as awaiting verification; it is not shown as a campus-wide active alert yet.
2. A security verifier confirms it, changing the status to `ACTIVE`. The dashboard then displays the alert and instructions tailored to each account’s role. Security can instead mark an unverified report `FALSE_ALARM`.
3. A guard responds using the guard instructions. When the incident is handled, the guard can submit a resolution note, changing the state to `RESOLUTION_REQUESTED`. Security can also close an active incident directly with a required resolution note.
4. Security reviews a guard's request. Approval changes the status to `RESOLVED` and removes the active map alert. Rejection returns the incident to `ACTIVE`. False alarms and resolved incidents remain in incident history.
5. Status changes and reviewer notes are recorded in the incident history.

## Build and tests

```powershell
Set-Location .\backend
mvn test
```

```powershell
Set-Location .\frontend
npm run lint
npm run build
```

## Limitations

This is a demonstration system, not a life-safety or dispatch platform. The floor plans support location selection only; they do not provide verified evacuation routing. Follow responders and approved emergency procedures. In-app polling is not a guaranteed alert channel. The crowd estimate is a rule-based prototype, not a validated ML model or live occupancy reading.
