#!/bin/bash
# ─────────────────────────────────────────────────────────────────────
# Lost & Found AI Agent — Run Script (Mac / Linux)
# ─────────────────────────────────────────────────────────────────────

set -e

# ── 1. Check for API key ───────────────────────────────────────────
if [ -z "$ANTHROPIC_API_KEY" ]; then
  echo ""
  echo "⚠️  ANTHROPIC_API_KEY is not set!"
  echo ""
  echo "   Get a free API key at: https://console.anthropic.com"
  echo "   Then run:  export ANTHROPIC_API_KEY=sk-ant-..."
  echo ""
  read -p "   Enter your API key now (or press Enter to skip AI features): " key
  if [ -n "$key" ]; then
    export ANTHROPIC_API_KEY="$key"
  fi
fi

# ── 2. Compile (only if needed) ───────────────────────────────────
if [ ! -d "out" ] || [ "$(find src -name '*.java' -newer out -print -quit 2>/dev/null)" ]; then
  echo "📦 Compiling..."
  mkdir -p out
  find src -name "*.java" > /tmp/sources.txt
  # Try javac first, fall back to javax.tools
  if command -v javac &> /dev/null; then
    javac --add-exports jdk.httpserver/sun.net.httpserver=ALL-UNNAMED \
          -d out @/tmp/sources.txt
  else
    java /tmp/Compile.java
  fi
  echo "✅ Compilation done"
fi

# ── 3. Run ─────────────────────────────────────────────────────────
echo ""
java --add-exports jdk.httpserver/sun.net.httpserver=ALL-UNNAMED \
     -cp out lostandfound.Main
