#!/bin/bash
# ====================================
# SCRIPT DE INSTALACIÓN INICIAL
# DigitalOcean Droplet - Ubuntu 22.04
# ====================================

set -e  # Salir si hay algún error

echo "======================================"
echo "Instalando Docker y dependencias..."
echo "======================================"

# Actualizar sistema
echo "→ Actualizando sistema..."
sudo apt-get update
sudo apt-get upgrade -y

# Instalar dependencias
echo "→ Instalando dependencias..."
sudo apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    git \
    ufw

# Agregar GPG key de Docker
echo "→ Agregando repositorio de Docker..."
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Agregar repositorio de Docker
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Instalar Docker
echo "→ Instalando Docker..."
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Agregar usuario actual al grupo docker
echo "→ Configurando permisos de Docker..."
sudo usermod -aG docker $USER

# Iniciar Docker
echo "→ Iniciando Docker..."
sudo systemctl start docker
sudo systemctl enable docker

# Configurar firewall
echo "→ Configurando firewall..."
sudo ufw --force enable
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow http
sudo ufw allow https

echo ""
echo "======================================"
echo "✅ Instalación completada!"
echo "======================================"
echo ""
echo "Verificando instalación..."
docker --version
docker compose version

echo ""
echo "⚠️  IMPORTANTE: Cierra sesión y vuelve a iniciar sesión"
echo "   para que los cambios de grupo de Docker tengan efecto."
echo ""
echo "Luego ejecuta: docker ps"
echo "Si funciona sin 'sudo', estás listo."
