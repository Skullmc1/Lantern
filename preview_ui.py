import http.server
import socketserver
import os
import json
import re
import uuid
import time
import subprocess
import webbrowser
import urllib.parse
import socket

# Configuration
PORT = 8000
VITE_PORT = 5173
MOCK_DIR = "mock_sd_card"

# Create mock directory with demo content (idempotent)
if not os.path.exists(MOCK_DIR):
    os.makedirs(MOCK_DIR)
if not os.path.exists(os.path.join(MOCK_DIR, "Welcome.txt")):
    with open(os.path.join(MOCK_DIR, "Welcome.txt"), "w") as f:
        f.write("Welcome to the Lantern Preview!\nThis is a file on your mock SD card.")
if not os.path.exists(os.path.join(MOCK_DIR, "Photo.jpg")):
    with open(os.path.join(MOCK_DIR, "Photo.jpg"), "w") as f:
        f.write("Binary content placeholder")
os.makedirs(os.path.join(MOCK_DIR, "Projects"), exist_ok=True)
if not os.path.exists(os.path.join(MOCK_DIR, "Projects", "notes.md")):
    with open(os.path.join(MOCK_DIR, "Projects", "notes.md"), "w") as f:
        f.write("# Project notes\n\nMock content for the preview.")
if not os.path.exists(os.path.join(MOCK_DIR, "Projects", "todo.txt")):
    with open(os.path.join(MOCK_DIR, "Projects", "todo.txt"), "w") as f:
        f.write("- Buy lantern oil\n- Water the meadow")

BASE_HTML = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{title}</title>
    <style>
        :root {{
            --lantern: #ffb84d;
            --lantern-soft: #ffe9b3;
            --lantern-deep: #d97706;
            --meadow: #34d399;
            --glass: rgba(16, 34, 27, 0.55);
            --glass-border: rgba(255, 214, 120, 0.16);
            --color-bg: #0e2a1a;
        }}
        * {{ box-sizing: border-box; }}
        body {{
            margin: 0;
            min-height: 100vh;
            background: var(--color-bg);
            color: #eef3ec;
            font-family: system-ui, -apple-system, sans-serif;
            display: flex;
            flex-direction: column;
            position: relative;
        }}
        .grass-plains {{
            position: fixed;
            inset: 0;
            overflow: hidden;
            pointer-events: none;
            z-index: 0;
        }}
        .sky {{
            position: absolute; inset: 0;
            background: linear-gradient(180deg, #0b1026 0%, #12294a 28%, #16404a 50%, #3d3a2c 66%, #7a4a1e 74%, #1d3a24 78%, #0e2a1a 100%);
        }}
        .horizon-glow {{
            position: absolute; left: 50%; top: 60%;
            transform: translate(-50%, -50%);
            width: 140vw; height: 55vh;
            background: radial-gradient(ellipse at center, rgba(255,190,90,0.5) 0%, rgba(255,150,60,0.22) 35%, transparent 70%);
            filter: blur(18px);
        }}
        .lantern-sun {{
            position: absolute; left: 50%; top: 62%;
            transform: translate(-50%, -50%);
            width: 150px; height: 150px; border-radius: 50%;
            background: radial-gradient(circle, #fff4cf 0%, #ffd88a 38%, #ffb84d 60%, transparent 74%);
            opacity: 0.92;
            box-shadow: 0 0 80px 30px rgba(255,184,77,0.35);
        }}
        .hill {{
            position: absolute; left: -10%; width: 120%;
            border-radius: 100% 100% 0 0;
        }}
        .hill-1 {{ height: 30%; bottom: 2%; background: linear-gradient(180deg, #17392a 0%, #12301f 100%); opacity: 0.9; }}
        .hill-2 {{ height: 20%; bottom: 14%; background: linear-gradient(180deg, #122d20 0%, #0e281a 100%); opacity: 0.85; }}
        .hill-3 {{ height: 13%; bottom: 26%; background: linear-gradient(180deg, #0e2519 0%, #0b1f15 100%); opacity: 0.8; }}
        .ground {{
            position: absolute; bottom: 0; left: 0; right: 0; height: 16vh;
            background: linear-gradient(180deg, #0d2416 0%, #0a1c12 60%, #081710 100%);
        }}
        .blades {{ position: absolute; inset: 0; }}
        .blade {{
            position: absolute; bottom: 0;
            transform-origin: bottom center;
            border-radius: 50% 50% 0 0 / 100% 100% 0 0;
            animation: sway 3s ease-in-out infinite alternate;
        }}
        @keyframes sway {{
            from {{ transform: rotate(calc(var(--tilt) - 5deg)) skewX(-4deg); }}
            to {{ transform: rotate(calc(var(--tilt) + 5deg)) skewX(4deg); }}
        }}
        .toolbar {{
            position: relative;
            z-index: 10;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-wrap: wrap;
            gap: 0.5rem 1rem;
            padding: 0.9rem 1rem;
            background: var(--glass);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border-bottom: 1px solid var(--glass-border);
        }}
        .toolbar .brand {{
            font-weight: 700;
            letter-spacing: 2px;
            color: var(--lantern-soft);
            margin-right: 1rem;
        }}
        .toolbar .group {{
            display: flex;
            gap: 0.4rem;
            align-items: center;
        }}
        .toolbar .group-label {{
            font-size: 0.7rem;
            color: #9fb4a8;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-right: 0.3rem;
        }}
        .toolbar a, .toolbar button {{
            background: rgba(255, 184, 77, 0.12);
            color: var(--lantern);
            border: 1px solid rgba(255, 184, 77, 0.3);
            padding: 0.45rem 1rem;
            border-radius: 50px;
            cursor: pointer;
            text-decoration: none;
            font-size: 0.8rem;
            font-weight: 600;
            transition: all 0.2s;
        }}
        .toolbar a:hover, .toolbar button:hover {{
            background: var(--lantern);
            color: #3a2406;
        }}
        .toolbar a.active {{
            background: var(--lantern);
            color: #3a2406;
        }}
        .container {{
            position: relative;
            z-index: 5;
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem;
        }}
        iframe {{
            border: none;
            background: var(--color-bg);
            transition: all 0.3s ease;
            box-shadow: 0 20px 50px rgba(0, 0, 0, 0.5);
        }}
        .mode-mobile iframe {{
            width: 375px;
            height: 812px;
            max-width: 100%;
            max-height: calc(100vh - 120px);
            border-radius: 44px;
            border: 10px solid #1a2320;
        }}
        .mode-web iframe {{
            width: 100%;
            height: 100%;
            max-width: 1300px;
            border-radius: 16px;
        }}
        .hub {{
            position: relative;
            z-index: 5;
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            gap: 2rem;
            padding: 2rem;
            text-align: center;
        }}
        .hub h1 {{
            font-size: clamp(1.8rem, 4vw, 2.6rem);
            margin: 0;
            background: linear-gradient(to right, #ffe9b3, #34d399);
            background-clip: text;
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }}
        .hub p {{ color: #b7cbbf; max-width: 480px; line-height: 1.6; }}
        .hub-cards {{
            display: flex;
            gap: 1.5rem;
            flex-wrap: wrap;
            justify-content: center;
        }}
        .hub-card {{
            background: var(--glass);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid var(--glass-border);
            border-radius: 20px;
            padding: 2rem 2.5rem;
            text-decoration: none;
            color: inherit;
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 1rem;
            min-width: 220px;
            transition: all 0.2s;
        }}
        .hub-card:hover {{
            transform: translateY(-4px);
            border-color: rgba(255, 184, 77, 0.5);
            box-shadow: 0 0 40px rgba(255, 184, 77, 0.2);
        }}
        .hub-card .emoji {{ font-size: 2.5rem; }}
        .hub-card .card-title {{ font-weight: 700; color: var(--lantern-soft); letter-spacing: 0.5px; }}
        .hub-card .card-desc {{ font-size: 0.85rem; color: #9fb4a8; }}
        .toast {{
            position: fixed;
            bottom: 1.5rem;
            left: 50%;
            transform: translateX(-50%);
            z-index: 100;
            background: var(--glass-strong, rgba(13,28,22,0.9));
            border: 1px solid var(--glass-border);
            border-radius: 12px;
            padding: 0.75rem 1.25rem;
            font-size: 0.85rem;
            color: var(--lantern-soft);
            backdrop-filter: blur(12px);
            box-shadow: 0 10px 30px rgba(0,0,0,0.4);
            animation: toastIn 0.3s ease;
        }}
        @keyframes toastIn {{
            from {{ opacity: 0; transform: translate(-50%, 10px); }}
            to {{ opacity: 1; transform: translate(-50%, 0); }}
        }}
    </style>
</head>
<body>
{content}
</body>
</html>
"""

def grass_markup(blade_count=80):
    import random
    random.seed(42)
    blades = []
    for i in range(blade_count):
        layer = i % 3
        base = 26 if layer == 0 else 42 if layer == 1 else 58
        spread = 20 if layer == 0 else 22 if layer == 1 else 34
        left = random.random() * 100
        height = base + random.random() * spread
        width = 4 + random.random() * 6
        delay = random.random() * -7
        dur = 2.4 + random.random() * 3.6
        tilt = (random.random() - 0.5) * 38
        lightness = 22 + layer * 7 + random.random() * 10
        blades.append(
            f'<span class="blade" style="left:{left:.1f}%;height:{height:.0f}px;'
            f'width:{width:.1f}px;--tilt:{tilt:.1f}deg;'
            f'animation-duration:{dur:.2f}s;animation-delay:{delay:.2f}s;'
            f'background:hsl(135 42% {lightness:.0f}%);"></span>'
        )
    return (
        '<div class="grass-plains" aria-hidden="true">'
        '<div class="sky"></div>'
        '<div class="horizon-glow"></div>'
        '<div class="lantern-sun"></div>'
        '<div class="hill hill-1"></div>'
        '<div class="hill hill-2"></div>'
        '<div class="hill hill-3"></div>'
        '<div class="ground"></div>'
        '<div class="blades">' + "".join(blades) + "</div>"
        "</div>"
    )

def toolbar_html(mobile_active="", web_active="", demos=""):
    return f"""
<div class="toolbar">
    <span class="brand">LANTERN</span>
    <a href="/">Hub</a>
    <a href="/preview/mobile" class="{mobile_active}">Mobile App</a>
    <a href="/preview/web" class="{web_active}">Web UI</a>
    {demos}
</div>
"""


class LanternPreviewHandler(http.server.SimpleHTTPRequestHandler):
    # Mock session store: sessionId -> (status, created_at)
    sessions = {}

    # ---------- helpers ----------

    def _send_json(self, obj, status=200):
        body = json.dumps(obj).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _session_id(self):
        headers = {k.lower(): v for k, v in self.headers.items()}
        return headers.get("x-session-id")

    def _resolve_path(self, rel):
        """Resolve a relative path inside MOCK_DIR safely."""
        if ".." in rel or rel.startswith("/") or ":" in rel:
            return None
        return os.path.join(MOCK_DIR, rel.replace("\\", "/"))

    # ---------- GET ----------

    def do_GET(self):
        parsed_url = urllib.parse.urlparse(self.path)
        path = parsed_url.path
        query = urllib.parse.parse_qs(parsed_url.query)

        if path == "/":
            self.send_response(200)
            self.send_header("Content-type", "text/html")
            self.end_headers()
            self.wfile.write(self.render_index().encode("utf-8"))
            return

        if path == "/preview/mobile":
            self._serve_preview("mobile", query)
            return

        if path == "/preview/web":
            self._serve_preview("web", query)
            return

        if path == "/api/list":
            self.handle_api_list(query)
            return

        if path == "/api/auth/status":
            self.handle_auth_status()
            return

        if path == "/api/download":
            self.handle_download(query)
            return

        if path.startswith("/api/share/list"):
            self.handle_api_list(query, shared=True)
            return

        # Fallback to default handler
        super().do_GET()

    # ---------- POST ----------

    def do_POST(self):
        parsed_url = urllib.parse.urlparse(self.path)
        path = parsed_url.path
        query = urllib.parse.parse_qs(parsed_url.query)

        if path == "/api/auth/request":
            self.handle_auth_request()
            return

        if path == "/api/rename":
            self.handle_rename()
            return

        if path == "/api/upload":
            self.handle_upload(query)
            return

        self._send_json({"error": "Not found"}, 404)

    # ---------- DELETE ----------

    def do_DELETE(self):
        parsed_url = urllib.parse.urlparse(self.path)
        path = parsed_url.path
        query = urllib.parse.parse_qs(parsed_url.query)

        if path == "/api/delete":
            self.handle_delete(query)
            return

        self._send_json({"error": "Not found"}, 404)

    # ---------- Preview pages ----------

    def _serve_preview(self, mode, query):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(self.render_preview(mode, query).encode("utf-8"))

    def render_index(self):
        content = grass_markup()
        content += toolbar_html()
        content += """
        <div class="hub">
            <h1>Lantern — Meadow Preview</h1>
            <p>Choose which surface you want to preview. The mobile app runs the same interface inside
            an Android WebView (phone frame + mock device bridge), while the web UI is the browser client.</p>
            <div class="hub-cards">
                <a class="hub-card" href="/preview/mobile">
                    <span class="emoji">📱</span>
                    <span class="card-title">Mobile App</span>
                    <span class="card-desc">Android ControlPanel in a phone frame, with a mocked device bridge.</span>
                </a>
                <a class="hub-card" href="/preview/web">
                    <span class="emoji">🖥️</span>
                    <span class="card-title">Web UI</span>
                    <span class="card-desc">Full-screen browser client — file browser and auth screens.</span>
                </a>
            </div>
        </div>
        """
        return BASE_HTML.format(title="Lantern — Preview Hub", content=content)

    def render_preview(self, mode, query):
        is_mobile = mode == "mobile"
        frame_class = "mode-mobile" if is_mobile else "mode-web"

        demo = query.get("demo", ["approved"])[0]
        if demo not in ("approved", "waiting", "rejected"):
            demo = "approved"

        if is_mobile:
            iframe_src = f"http://localhost:{VITE_PORT}?mode=android"
        else:
            iframe_src = f"http://localhost:{VITE_PORT}?demo={demo}"

        demos = ""
        if not is_mobile:
            demos = (
                '<span class="group"><span class="group-label">Auth:</span>'
                f'<a href="/preview/web?demo=approved" class="{"active" if demo == "approved" else ""}">Approved</a>'
                f'<a href="/preview/web?demo=waiting" class="{"active" if demo == "waiting" else ""}">Waiting</a>'
                f'<a href="/preview/web?demo=rejected" class="{"active" if demo == "rejected" else ""}">Rejected</a>'
                "</span>"
            )

        content = grass_markup()
        content += toolbar_html(
            mobile_active="active" if is_mobile else "",
            web_active="active" if not is_mobile else "",
            demos=demos,
        )
        content += (
            f'<div class="container {frame_class}">'
            f'<iframe id="frame" src="{iframe_src}"></iframe>'
            "</div>"
        )
        title = "Lantern — Mobile App Preview" if is_mobile else "Lantern — Web UI Preview"
        return BASE_HTML.format(title=title, content=content)

    # ---------- Mock API: auth ----------

    def handle_auth_request(self):
        try:
            length = int(self.headers.get("Content-Length", 0))
            raw = self.rfile.read(length) if length else b"{}"
            body = json.loads(raw.decode("utf-8") or "{}")
        except Exception:
            body = {}

        device_name = body.get("deviceName", "Web Client")
        sid = "demo-" + uuid.uuid4().hex[:8]

        # Enforce the demo state that was chosen via ?demo= on the preview page.
        # The iframe URL carries ?demo=, so we read it from the Referer's query.
        demo = "approved"
        referer = self.headers.get("Referer", "")
        if "?demo=" in referer:
            qs = referer.split("?", 1)[1]
            demo = urllib.parse.parse_qs(qs).get("demo", ["approved"])[0]

        status = "PENDING" if demo == "waiting" else "APPROVED" if demo == "approved" else "REJECTED"
        LanternPreviewHandler.sessions[sid] = (status, time.time())
        print(f"[Mock] auth request: device={device_name} session={sid} status={status}")
        self._send_json({"sessionId": sid, "status": status, "deviceName": device_name})

    def handle_auth_status(self):
        sid = self._session_id()
        if not sid or sid not in LanternPreviewHandler.sessions:
            self._send_json({"error": "Unauthorized"}, 401)
            return

        status, created = LanternPreviewHandler.sessions[sid]
        # Auto-approve a "waiting" demo after a few seconds so the transition is visible.
        if status == "PENDING" and time.time() - created > 6:
            status = "APPROVED"
            LanternPreviewHandler.sessions[sid] = (status, created)

        self._send_json({"sessionId": sid, "status": status})

    # ---------- Mock API: files ----------

    def handle_api_list(self, query, shared=False):
        req_path = query.get("path", [""])[0]
        target_dir = self._resolve_path(req_path)
        if target_dir is None or not os.path.exists(target_dir):
            self._send_json({"error": "Not found"}, 404)
            return

        files = []
        try:
            for entry in os.scandir(target_dir):
                if entry.name.startswith("."):
                    continue
                files.append({
                    "name": entry.name,
                    "isDirectory": entry.is_dir(),
                    "size": entry.stat().st_size,
                })
        except Exception as e:
            print(f"Error listing files: {e}")

        files.sort(key=lambda x: (not x["isDirectory"], x["name"].lower()))
        self._send_json({"path": req_path, "files": files})

    def handle_download(self, query):
        rel = query.get("path", [""])[0]
        target = self._resolve_path(rel)
        if target is None or not os.path.isfile(target):
            self._send_json({"error": "Not found"}, 404)
            return

        with open(target, "rb") as f:
            data = f.read()
        self.send_response(200)
        self.send_header("Content-type", "application/octet-stream")
        self.send_header("Content-Disposition", f'attachment; filename="{os.path.basename(target)}"')
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def handle_upload(self, query):
        length = int(self.headers.get("Content-Length", 0))
        raw = self.rfile.read(length) if length else b""
        fields = parse_multipart(self.headers.get("Content-Type", ""), raw)

        file_item = fields.get("file")
        if not file_item or not file_item[1]:
            self._send_json({"error": "No file provided"}, 400)
            return

        filename = os.path.basename(file_item[0] or "upload.bin")
        req_path = query.get("path", [""])[0]
        target_dir = self._resolve_path(req_path)
        if target_dir is None:
            self._send_json({"error": "Forbidden"}, 403)
            return
        os.makedirs(target_dir, exist_ok=True)
        target = os.path.join(target_dir, filename)
        with open(target, "wb") as f:
            f.write(file_item[1])
        print(f"[Mock] uploaded {filename} -> {target}")
        self._send_json({"ok": True, "name": filename})

    def handle_rename(self):
        try:
            length = int(self.headers.get("Content-Length", 0))
            body = json.loads(self.rfile.read(length).decode("utf-8"))
        except Exception:
            self._send_json({"error": "Bad request"}, 400)
            return

        src = self._resolve_path(body.get("path", ""))
        new_name = os.path.basename(body.get("newName", ""))
        if src is None or not os.path.exists(src) or not new_name:
            self._send_json({"error": "Bad request"}, 400)
            return
        dst = os.path.join(os.path.dirname(src), new_name)
        os.rename(src, dst)
        self._send_json({"ok": True})

    def handle_delete(self, query):
        rel = query.get("path", [""])[0]
        target = self._resolve_path(rel)
        if target is None or not os.path.exists(target):
            self._send_json({"error": "Not found"}, 404)
            return
        if os.path.isdir(target):
            import shutil
            shutil.rmtree(target)
        else:
            os.remove(target)
        self._send_json({"ok": True})


def port_in_use(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        return s.connect_ex(("127.0.0.1", port)) == 0


def parse_multipart(content_type, data):
    """Parse a multipart/form-data body into {name: (filename, bytes)}."""
    m = re.search(r'boundary=(?:"([^"]+)"|([^;]+))', content_type or "")
    if not m:
        return {}
    boundary = (m.group(1) or m.group(2)).encode("utf-8")
    parts = {}
    for chunk in data.split(b"--" + boundary)[1:-1]:
        chunk = chunk.lstrip(b"\r\n")
        header_end = chunk.find(b"\r\n\r\n")
        if header_end == -1:
            continue
        header_block = chunk[:header_end].decode("utf-8", "replace")
        body = chunk[header_end + 4:]
        if body.endswith(b"\r\n"):
            body = body[:-2]
        name = None
        filename = None
        for line in header_block.split("\r\n"):
            if line.lower().startswith("content-disposition:"):
                dm = re.search(r'name="([^"]*)"', line)
                fm = re.search(r'filename="([^"]*)"', line)
                if dm:
                    name = dm.group(1)
                if fm:
                    filename = fm.group(1)
        if name:
            parts[name] = (filename, body)
    return parts


def start_vite():
    if port_in_use(VITE_PORT):
        print(f"Vite already running on :{VITE_PORT}, reusing it.")
        return

    print("Starting Vite server...")
    try:
        subprocess.run(["bun", "--version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
        cmd = ["bun", "run", "dev"]
    except Exception:
        print("Bun not found, trying npm...")
        cmd = ["npm", "run", "dev"]

    cwd = os.path.join(os.getcwd(), "web")
    try:
        subprocess.Popen(cmd, cwd=cwd, shell=True)
    except Exception as e:
        print(f"Failed to start Vite: {e}")


def main():
    start_vite()

    print(f"Starting Preview Server on http://localhost:{PORT}")
    print(f"Vite should be running on http://localhost:{VITE_PORT}")
    print()
    print("  Preview Hub:       http://localhost:8000/")
    print("  Mobile App:        http://localhost:8000/preview/mobile")
    print("  Web UI:            http://localhost:8000/preview/web")

    with socketserver.TCPServer(("", PORT), LanternPreviewHandler) as httpd:
        print(f"Open http://localhost:{PORT} to view the app.")
        webbrowser.open(f"http://localhost:{PORT}")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nStopping server...")
            httpd.shutdown()


if __name__ == "__main__":
    main()
