#!/bin/bash
# ====================================
# CONFIGURAR SSL CON LET'S ENCRYPT
# ====================================

set -e

# Cargar variables de entorno
if [ -f ".env.prod" ]; then
    source .env.prod
else
    echo "❌ ERROR: No existe .env.prod"
    exit 1
fi

# Verificar que las variables están configuradas
if [ -z "$DOMAIN" ] || [ -z "$SSL_EMAIL" ]; then
    echo "❌ ERROR: DOMAIN y SSL_EMAIL deben estar configurados en .env.prod"
    exit 1
fi

echo "======================================"
echo "🔐 Configurando SSL para: $DOMAIN"
echo "======================================"

# Crear directorios si no existen
mkdir -p certbot/conf certbot/www

# Obtener certificado
echo "→ Solicitando certificado SSL..."
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email $SSL_EMAIL \
    --agree-tos \
    --no-eff-email \
    -d $DOMAIN \
    -d www.$DOMAIN

echo ""
echo "======================================"
echo "✅ Certificado SSL obtenido!"
echo "======================================"
echo ""
echo "⚠️  IMPORTANTE:"
echo "1. Edita nginx/conf.d/app.conf y reemplaza 'tu-dominio.com' con '$DOMAIN'"
echo "2. Reinicia nginx: docker compose -f docker-compose.prod.yml restart nginx"
echo ""
echo "El certificado se renovará automáticamente cada 12 horas."
