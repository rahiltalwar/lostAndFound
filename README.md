# 🎒 Lost & Found AI Agent — School Edition

An AI-powered Lost & Found management system built with **pure Java + Maven**.  
Upload photos of found items, let **Claude AI** describe them, and search using **natural language**.

---

## ✨ Features

| Feature | Description |
|---|---|
| 📷 **Image Upload** | Upload photos of found items |
| 🤖 **AI Image Analysis** | Claude AI auto-describes each item |
| 🔍 **Natural Language Search** | "red water bottle with Ankit on it" |
| 📦 **Browse All Items** | Card grid with status tracking |
| ✅ **Mark as Claimed** | Track who collected what |
| 💾 **Persistent Storage** | Items saved as JSON files — no database server needed |

---

## 🚀 Running in IntelliJ IDEA

### Step 1 — Open the project
1. Open IntelliJ IDEA
2. Click **File → Open** and select the `lostandfound` folder
3. IntelliJ will detect the `pom.xml` automatically and import it as a Maven project
4. Wait for Maven to finish (bottom progress bar)

### Step 2 — Set your Anthropic API key
You need a free API key from [console.anthropic.com](https://console.anthropic.com).

In IntelliJ:
1. Open **Run → Edit Configurations…**
2. Select (or create) the `Main` run configuration
3. Click **Modify options → Environment variables**
4. Add: `ANTHROPIC_API_KEY=sk-ant-YOUR-KEY-HERE`
5. Click **OK**

### Step 3 — Run
- Open `src/main/java/lostandfound/Main.java`
- Click the ▶ green arrow next to `public static void main`
- Open your browser at **http://localhost:8080**

> The `.mvn/jvm.config` file already handles the `--add-exports` flag,
> so the green run button works without any extra VM options.

---

## 🖥️ Running from the terminal

```bash
# Mac / Linux
export ANTHROPIC_API_KEY=sk-ant-YOUR-KEY-HERE
mvn exec:java

# Windows
set ANTHROPIC_API_KEY=sk-ant-YOUR-KEY-HERE
mvn exec:java
```

Or build a standalone fat JAR and run it anywhere:
```bash
mvn package
java --add-exports jdk.httpserver/sun.net.httpserver=ALL-UNNAMED \
     -jar target/lostandfound-1.0.0.jar
```

---

## 📁 Project Structure

```
lostandfound/
├── pom.xml                          ← Maven project file
├── .mvn/
│   └── jvm.config                   ← Auto-applies --add-exports for IntelliJ
├── src/main/java/lostandfound/
│   ├── Main.java                    ← Entry point, starts HTTP server on :8080
│   ├── Database.java                ← JSON file storage (saves to ./data/)
│   ├── LostItem.java                ← Data model + JSON serialization
│   ├── ClaudeService.java           ← Anthropic API (image analysis + search)
│   ├── Handlers.java                ← HTTP request handlers
│   └── Frontend.java                ← Entire web UI as an HTML string
├── data/                            ← Auto-created at runtime
│   ├── items/                       ← One .json file per lost item
│   └── images/                      ← Uploaded item photos
└── README.md
```

---

## 🔌 API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/` | Web UI |
| `GET` | `/api/items` | List all items |
| `POST` | `/api/upload` | Add a new found item (with optional image) |
| `POST` | `/api/search` | AI natural language search |
| `POST` | `/api/items/{id}/status` | Mark as claimed / unclaimed |
| `GET` | `/images/{filename}` | Serve item photos |

---

## ⚙️ Configuration

| Setting | Where | Default |
|---|---|---|
| Server port | `Main.java` → `PORT` | `8080` |
| Claude model | `ClaudeService.java` → `MODEL` | `claude-opus-4-6` |
| API key | Environment variable `ANTHROPIC_API_KEY` | — |

---

## 🛠️ No External Runtime Dependencies

Only the Java Standard Library is used at runtime:
- `jdk.httpserver` — built-in HTTP server
- `java.net.http.HttpClient` — built-in HTTP client for Anthropic API calls
- `java.nio.file` — file I/O for JSON + image storage

Maven is only needed to **build**; the resulting JAR runs with plain `java`.
