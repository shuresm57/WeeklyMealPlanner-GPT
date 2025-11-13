#!/bin/bash

# Environment Variables Verification Script
# This script checks if all required environment variables are set

echo "=========================================="
echo "Environment Variables Verification"
echo "=========================================="
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Required variables
required_vars=(
    "MAIL_USERNAME"
    "MAIL_PASSWORD"
    "GOOGLE_CLIENT_ID"
    "GOOGLE_CLIENT_SECRET"
    "OPENAI_API_KEY"
)

# Optional variables (for Docker Compose)
optional_vars=(
    "DB_URL"
    "DB_USERNAME"
    "DB_PASSWORD"
    "DB_NAME"
    "SPRING_PROFILES_ACTIVE"
)

missing_count=0
optional_missing=0

echo "Checking required environment variables:"
echo ""

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo -e "${RED}✗${NC} $var is NOT set"
        missing_count=$((missing_count + 1))
    else
        # Mask the value for security
        value="${!var}"
        if [ ${#value} -gt 10 ]; then
            masked="${value:0:4}...${value: -4}"
        else
            masked="****"
        fi
        echo -e "${GREEN}✓${NC} $var is set: $masked"
    fi
done

echo ""
echo "Checking optional environment variables (for Docker Compose):"
echo ""

for var in "${optional_vars[@]}"; do
    if [ -z "${!var}" ]; then
        echo -e "${YELLOW}○${NC} $var is NOT set (optional)"
        optional_missing=$((optional_missing + 1))
    else
        value="${!var}"
        if [ ${#value} -gt 10 ]; then
            masked="${value:0:4}...${value: -4}"
        else
            masked="****"
        fi
        echo -e "${GREEN}✓${NC} $var is set: $masked"
    fi
done

echo ""
echo "=========================================="
echo "Summary"
echo "=========================================="

if [ $missing_count -eq 0 ]; then
    echo -e "${GREEN}✓ All required environment variables are set!${NC}"
    echo ""
    echo "You can now run the application with:"
    echo "  ./mvnw spring-boot:run"
else
    echo -e "${RED}✗ $missing_count required environment variable(s) missing${NC}"
    echo ""
    echo "To fix this:"
    echo "  1. Copy .env.example to .env: cp .env.example .env"
    echo "  2. Edit .env and add your credentials"
    echo "  3. Source the .env file: set -a; source .env; set +a"
    echo "  4. Run this script again to verify"
    exit 1
fi

if [ $optional_missing -gt 0 ]; then
    echo -e "${YELLOW}Note: $optional_missing optional variable(s) not set (required for Docker Compose)${NC}"
fi

echo ""
