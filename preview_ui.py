import http.server
import socketserver
import os
import json
import subprocess
import threading
import webbrowser
import urllib.parse
import time
import sys

# Configuration
PORT = 8000
VITE_PORT = 5173
MOCK_DIR = "mock_sd_card"

# Create mock directory
if not os.path.exists(MOCK_DIR):
    os.makedirs(MOCK_DIR)
    with open(os.path.join(MOCK_DIR, "Welcome.txt"), "w") as f:
        f.write("Welcome to the Lantern Preview!\nThis is a file on your mock SD card.")
    with open(os.path.join(MOCK_DIR, "Photo.jpg"), "w") as f:
        f.write("Binary content placeholder")

class LanternPreviewHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        parsed_url = urllib.parse.urlparse(self.path)
        path = parsed_url.path
        query = urllib.parse.parse_qs(parsed_url.query)

        if path == "/":
            self.send_response(200)
            self.send_header("Content-type", "text/html")
            self.end_headers()
            self.wfile.write(self.get_html_content().encode("utf-8"))
            return
        
        if path == "/api/list":
            self.handle_api_list(query)
            return

        # Fallback to default handler (unlikely needed as we don't serve static files directly, Vite does)
        super().do_GET()

    def handle_api_list(self, query):
        req_path = query.get("path", [""])[0]
        # Security: Prevent escaping mock dir
        if ".." in req_path:
            self.send_error(403, "Forbidden")
            return

        target_dir = os.path.join(MOCK_DIR, req_path)
        
        if not os.path.exists(target_dir):
            self.send_response(404)
            self.end_headers()
            return

        files = []
        try:
            for entry in os.scandir(target_dir):
                files.append({
                    "name": entry.name,
                    "isDirectory": entry.is_dir(),
                    "size": entry.stat().st_size
                })
        except Exception as e:
            print(f"Error listing files: {e}")
        
        # Sort: Directories first, then files
        files.sort(key=lambda x: (not x["isDirectory"], x["name"]))

        response_data = {
            "path": req_path,
            "files": files
        }

        self.send_response(200)
        self.send_header("Content-type", "application/json")
        self.send_header("Access-Control-Allow-Origin", "*") # Allow Vite to access
        self.end_headers()
        self.wfile.write(json.dumps(response_data).encode("utf-8"))

    def get_html_content(self):
        return f"""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lantern UI Preview</title>
    <style>
        body {{
            margin: 0;
            padding: 0;
            background: #0f172a;
            color: white;
            font-family: system-ui, -apple-system, sans-serif;
            display: flex;
            flex-direction: column;
            height: 100vh;
        }}
        .toolbar {{
            padding: 1rem;
            background: #1e293b;
            border-bottom: 1px solid #334155;
            display: flex;
            gap: 1rem;
            align-items: center;
            justify-content: center;
        }}
        button {{
            background: #334155;
            color: white;
            border: 1px solid #475569;
            padding: 0.5rem 1rem;
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;
        }}
        button:hover {{ background: #475569; }}
        button.active {{
            background: #10b981;
            border-color: #059669;
            color: #022c22;
            font-weight: bold;
        }}
        .container {{
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem;
            background: #020617; 
        }}
        iframe {{
            border: none;
            background: white;
            transition: all 0.3s ease;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5);
        }}
        
        /* Android Phone Mode */
        .mode-android iframe {{
            width: 375px;
            height: 812px;
            border-radius: 40px;
            border: 8px solid #333;
        }}

        /* Web Client Mode */
        .mode-client iframe {{
            width: 100%;
            height: 100%;
            border-radius: 8px;
            max-width: 1200px;
        }}

        .hidden {{ display: none; }}
    </style>
</head>
<body>
    <div class="toolbar">
        <button onclick="setMode('android')" id="btn-android" class="active">Android App</button>
        <button onclick="setMode('client')" id="btn-client">Web Client</button>
    </div>
    
    <div class="container mode-android" id="viewport">
        <iframe id="frame" src="http://localhost:{VITE_PORT}?mode=android"></iframe>
    </div>

    <script>
        const viewport = document.getElementById('viewport');
        const frame = document.getElementById('frame');
        const btnAndroid = document.getElementById('btn-android');
        const btnClient = document.getElementById('btn-client');

        function setMode(mode) {{
            if (mode === 'android') {{
                viewport.className = 'container mode-android';
                frame.src = "http://localhost:{VITE_PORT}?mode=android";
                btnAndroid.classList.add('active');
                btnClient.classList.remove('active');
            }} else {{
                viewport.className = 'container mode-client';
                frame.src = "http://localhost:{VITE_PORT}"; // No mode param = client
                btnAndroid.classList.remove('active');
                btnClient.classList.add('active');
            }}
        }}
    </script>
</body>
</html>
        """

def start_vite():
    print("Starting Vite server...")
    # Using 'bun' as per user memory
    try:
        # Check if bun is available
        subprocess.run(["bun", "--version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
        cmd = ["bun", "run", "dev"]
    except:
        print("Bun not found, trying npm...")
        cmd = ["npm", "run", "dev"]

    # Run in web directory
    cwd = os.path.join(os.getcwd(), "web")
    try:
        subprocess.Popen(cmd, cwd=cwd, shell=True)
    except Exception as e:
        print(f"Failed to start Vite: {e}")

def main():
    # Start Vite in a separate thread/process
    start_vite()

    print(f"Starting Preview Server on http://localhost:{PORT}")
    print(f"Vite should be running on http://localhost:{VITE_PORT}")
    
    with socketserver.TCPServer(("", PORT), LanternPreviewHandler) as httpd:
        print(f"Open http://localhost:{PORT} to view the app.")
        # Open browser automatically
        webbrowser.open(f"http://localhost:{PORT}")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nStopping server...")
            httpd.shutdown()

if __name__ == "__main__":
    main()
