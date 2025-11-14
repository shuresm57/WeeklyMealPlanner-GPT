#!/bin/bash

echo "=== Complete Docker Rebuild Script ==="
echo ""

cd "$(dirname "$0")"

echo "Step 1: Stopping containers..."
docker compose down -v

echo ""
echo "Step 2: Removing old image..."
docker rmi weeklymealplanner-gpt-app --force 2>/dev/null || echo "Image not found, continuing..."

echo ""
echo "Step 3: Cleaning Maven build..."
./mvnw clean

echo ""
echo "Step 4: Pruning Docker build cache..."
docker builder prune -f

echo ""
echo "Step 5: Building fresh image (this may take a few minutes)..."
docker compose build --no-cache --pull

echo ""
echo "Step 6: Starting containers..."
docker compose up -d

echo ""
echo "Step 7: Waiting for application to start..."
sleep 10

echo ""
echo "Step 8: Showing logs (Ctrl+C to exit)..."
docker compose logs -f app
