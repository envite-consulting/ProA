#!/bin/bash

# Set paths for the private and public key files
PRIVATE_KEY_PATH="src/main/resources/privateKey.pem"
PUBLIC_KEY_PATH="src/main/resources/publicKey.pem"

# Check if the private and public key files exist, and generate if they do not
if [[ ! -f "$PRIVATE_KEY_PATH" ]] || [[ ! -f "$PUBLIC_KEY_PATH" ]]; then
  echo "Generating RSA key pair..."

  # Create directories if they do not exist
  mkdir -p "$(dirname "$PRIVATE_KEY_PATH")"

  # Generate RSA key pair using openssl
  openssl genpkey -algorithm RSA -out "$PRIVATE_KEY_PATH" -pkeyopt rsa_keygen_bits:2048
  openssl rsa -pubout -in "$PRIVATE_KEY_PATH" -out "$PUBLIC_KEY_PATH"

  # Convert the private and public keys to Base64 format and write to files
  PRIV_KEY_BASE64=$(base64 < "$PRIVATE_KEY_PATH")
  PUB_KEY_BASE64=$(base64 < "$PUBLIC_KEY_PATH")

  # Format the private key in PEM format with Base64 encoding
  echo "-----BEGIN PRIVATE KEY-----" > "$PRIVATE_KEY_PATH"
  echo "$PRIV_KEY_BASE64" >> "$PRIVATE_KEY_PATH"
  echo "-----END PRIVATE KEY-----" >> "$PRIVATE_KEY_PATH"

  # Format the public key in PEM format with Base64 encoding
  echo "-----BEGIN PUBLIC KEY-----" > "$PUBLIC_KEY_PATH"
  echo "$PUB_KEY_BASE64" >> "$PUBLIC_KEY_PATH"
  echo "-----END PUBLIC KEY-----" >> "$PUBLIC_KEY_PATH"

  echo "Key pair generated successfully."

else
  echo "Keys already exist. Skipping generation."
fi
