#!/bin/bash

# Simple script to run the Gantt Demo
cd /home/r-uu/develop/github/java

echo "🚀 Building Gantt Demo..."
mvn -pl lib/fx/control/gantt/core,lib/fx/control/gantt/demo -DskipTests clean package -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"
echo ""
echo "📍 Launching Gantt Chart Demo..."
echo ""

# Run with modular classpath
java \
  --module-path lib/fx/control/gantt/demo/target/r-uu.lib.fx.control.gantt.demo-0.0.1.jar:~/.m2/repository \
  --add-modules de.ruu.lib.fx.control.gantt.demo \
  -m de.ruu.lib.fx.control.gantt.demo/de.ruu.lib.fx.control.gantt.demo.GanttDemoStandalone

