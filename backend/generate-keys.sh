#!/bin/bash

# Get the absolute path of the script directory (project root)
SCRIPT_DIR="$(dirname "$(realpath "$0")")"

# Set paths for the private and public key files
PRIVATE_KEY_RSA_PATH="$SCRIPT_DIR/src/main/resources/rsaPrivateKey.pem"
PRIVATE_KEY_PATH="$SCRIPT_DIR/src/main/resources/privateKey.pem"
PUBLIC_KEY_PATH="$SCRIPT_DIR/src/main/resources/publicKey.pem"

# Check if the private and public key files exist, and generate if they do not
if [[ ! -f "$PRIVATE_KEY_PATH" ]] || [[ ! -f "$PUBLIC_KEY_PATH" ]]; then
  echo "Generating key pair..."

  # Create directories if they do not exist
  mkdir -p "$(dirname "$PRIVATE_KEY_PATH")"

  # Generate RSA key pair using openssl
  openssl genrsa -out "$PRIVATE_KEY_RSA_PATH" 2048
  openssl rsa -pubout -in "$PRIVATE_KEY_RSA_PATH" -out "$PUBLIC_KEY_PATH"

  openssl pkcs8 -topk8 -nocrypt -inform pem -in "$PRIVATE_KEY_RSA_PATH" -outform pem -out "$PRIVATE_KEY_PATH"

  echo "Key pair generated successfully."

else
  echo "Keys already exist. Skipping generation."
fi

TEST_PRIVATE_KEY_RSA_PATH="$SCRIPT_DIR/src/main/resources/rsaPrivateKey.test..pem"
TEST_PRIVATE_KEY_PATH="$SCRIPT_DIR/src/main/resources/privateKey.test.pem"
TEST_PUBLIC_KEY_PATH="$SCRIPT_DIR/src/main/resources/publicKey.test.pem"

# Check if the private and public key files exist, and generate if they do not
if [[ ! -f "$TEST_PRIVATE_KEY_PATH" ]] || [[ ! -f "$TEST_PUBLIC_KEY_PATH" ]]; then
  echo "Generating test key pair..."

  # Create directories if they do not exist
  mkdir -p "$(dirname "$TEST_PRIVATE_KEY_PATH")"

  # Generate RSA key pair using openssl
  openssl genrsa -out "$TEST_PRIVATE_KEY_RSA_PATH" 2048
  openssl rsa -pubout -in "$TEST_PRIVATE_KEY_RSA_PATH" -out "$TEST_PUBLIC_KEY_PATH"

  openssl pkcs8 -topk8 -nocrypt -inform pem -in "$TEST_PRIVATE_KEY_RSA_PATH" -outform pem -out "$TEST_PRIVATE_KEY_PATH"

  echo "Test key pair generated successfully."

else
  echo "Test keys already exist. Skipping generation."
fi

if [[ -f "$PRIVATE_KEY_RSA_PATH" ]]; then
  rm "$PRIVATE_KEY_RSA_PATH"
fi

if [[ -f "$TEST_PRIVATE_KEY_RSA_PATH" ]]; then
  rm "$TEST_PRIVATE_KEY_RSA_PATH"
fi
