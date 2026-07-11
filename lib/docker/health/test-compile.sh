#!/bin/bash
cd /home/r-uu/develop/github/main/root/lib/docker.health
echo "════════════════════════════════════════════════════════════════"
echo "Kompiliere docker.health Modul..."
echo "════════════════════════════════════════════════════════════════"
mvn clean compile 2>&1 | tail -50
