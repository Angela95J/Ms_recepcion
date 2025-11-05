-- ============================================
  -- MICROSERVICIO: RECEPCIÓN DE INCIDENTES
  -- Esquema optimizado sin redundancias
  -- ============================================

  -- 1. SOLICITANTE
  CREATE TABLE solicitante (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      nombre_completo VARCHAR(150) NOT NULL,
      telefono VARCHAR(20) NOT NULL,
      canal_origen VARCHAR(50) NOT NULL CHECK (canal_origen IN ('whatsapp', 'telegram')),
      fecha_registro TIMESTAMP DEFAULT NOW(),
      CONSTRAINT uk_solicitante_telefono UNIQUE (telefono)
  );

  CREATE INDEX idx_solicitante_telefono ON solicitante(telefono);
  CREATE INDEX idx_solicitante_canal ON solicitante(canal_origen);

  -- 2. UBICACION
  CREATE TABLE ubicacion (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      descripcion_textual TEXT NOT NULL,
      referencia TEXT,
      latitud DECIMAL(10, 8),
      longitud DECIMAL(11, 8),
      ciudad VARCHAR(100) DEFAULT 'Santa Cruz de la Sierra',
      distrito VARCHAR(100),
      zona VARCHAR(100),
      fecha_creacion TIMESTAMP DEFAULT NOW(),
      CONSTRAINT chk_coordenadas_validas CHECK (
          (latitud IS NULL AND longitud IS NULL) OR
          (latitud BETWEEN -90 AND 90 AND longitud BETWEEN -180 AND 180)
      )
  );

  CREATE INDEX idx_ubicacion_coordenadas ON ubicacion(latitud, longitud);
  CREATE INDEX idx_ubicacion_distrito ON ubicacion(distrito);

  -- 3. INCIDENTE
  CREATE TABLE incidente (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

      -- Foreign Keys
      solicitante_id UUID NOT NULL REFERENCES solicitante(id) ON DELETE CASCADE,
      ubicacion_id UUID NOT NULL REFERENCES ubicacion(id) ON DELETE CASCADE,
      analisis_texto_id UUID REFERENCES analisis_ml_texto(id),

      -- Datos del incidente
      descripcion_original TEXT NOT NULL,
      tipo_incidente_reportado VARCHAR(100),
      tipo_incidente_clasificado VARCHAR(100),

      -- Prioridad calculada por ML
      prioridad_inicial INT DEFAULT 3 CHECK (prioridad_inicial BETWEEN 1 AND 5),
      prioridad_texto INT CHECK (prioridad_texto BETWEEN 1 AND 5),
      prioridad_imagen INT CHECK (prioridad_imagen BETWEEN 1 AND 5),
      prioridad_final INT CHECK (prioridad_final BETWEEN 1 AND 5),

      -- Veracidad (análisis de imagen)
      score_veracidad DECIMAL(5, 4),
      es_verosimil BOOLEAN,

      -- Estado del incidente
      estado_incidente VARCHAR(30) DEFAULT 'RECIBIDO'
          CHECK (estado_incidente IN (
              'RECIBIDO',
              'EN_ANALISIS_TEXTO',
              'EN_ANALISIS_IMAGEN',
              'ANALIZADO',
              'APROBADO',
              'RECHAZADO',
              'CANCELADO'
          )),

      motivo_rechazo TEXT,

      -- Auditoría
      fecha_reporte TIMESTAMP DEFAULT NOW(),
      fecha_analisis_completado TIMESTAMP,
      fecha_ultima_actualizacion TIMESTAMP DEFAULT NOW(),
      observaciones TEXT
  );

  CREATE INDEX idx_incidente_estado ON incidente(estado_incidente);
  CREATE INDEX idx_incidente_prioridad_final ON incidente(prioridad_final) WHERE prioridad_final IS NOT NULL;
  CREATE INDEX idx_incidente_veracidad ON incidente(score_veracidad) WHERE score_veracidad IS NOT NULL;
  CREATE INDEX idx_incidente_fecha ON incidente(fecha_reporte);
  CREATE INDEX idx_incidente_solicitante ON incidente(solicitante_id);

  -- 4. MULTIMEDIA
  CREATE TABLE multimedia (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      incidente_id UUID NOT NULL REFERENCES incidente(id) ON DELETE CASCADE,

      -- Archivo
      url_archivo TEXT NOT NULL,
      url_miniatura TEXT,
      nombre_archivo VARCHAR(255),
      tipo_archivo VARCHAR(20) DEFAULT 'imagen'
          CHECK (tipo_archivo IN ('imagen', 'audio', 'video')),
      formato_archivo VARCHAR(10),
      tamano_bytes BIGINT,

      -- Metadatos
      descripcion TEXT,
      es_principal BOOLEAN DEFAULT FALSE,

      -- Estado de análisis
      requiere_analisis_ml BOOLEAN DEFAULT TRUE,
      analisis_completado BOOLEAN DEFAULT FALSE,

      fecha_subida TIMESTAMP DEFAULT NOW()
  );

  CREATE INDEX idx_multimedia_incidente ON multimedia(incidente_id);
  CREATE INDEX idx_multimedia_tipo ON multimedia(tipo_archivo);
  CREATE INDEX idx_multimedia_principal ON multimedia(es_principal) WHERE es_principal = TRUE;
  CREATE INDEX idx_multimedia_pendiente ON multimedia(analisis_completado)
      WHERE requiere_analisis_ml = TRUE AND analisis_completado = FALSE;

  -- 5. ANALISIS_ML_TEXTO
  CREATE TABLE analisis_ml_texto (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

      -- Datos de entrada
      texto_analizado TEXT NOT NULL,

      -- Resultados del modelo
      prioridad_calculada INT NOT NULL CHECK (prioridad_calculada BETWEEN 1 AND 5),
      nivel_gravedad INT NOT NULL CHECK (nivel_gravedad BETWEEN 1 AND 5),
      tipo_incidente_predicho VARCHAR(100) NOT NULL,

      -- Detecciones
      categorias_detectadas JSONB NOT NULL,
      palabras_clave_criticas JSONB NOT NULL,
      entidades_medicas JSONB,

      -- Métricas del modelo
      score_confianza DECIMAL(5, 4) NOT NULL,
      probabilidades_categorias JSONB,

      -- Metadatos del modelo
      modelo_version VARCHAR(50) DEFAULT 'bert-medical-v1.0',
      algoritmo_usado VARCHAR(50) DEFAULT 'transformer',
      tiempo_procesamiento_ms INT,

      -- Auditoría
      fecha_analisis TIMESTAMP DEFAULT NOW(),
      estado_analisis VARCHAR(20) DEFAULT 'COMPLETADO'
          CHECK (estado_analisis IN ('PENDIENTE', 'PROCESANDO', 'COMPLETADO', 'ERROR')),
      error_mensaje TEXT
  );

  CREATE INDEX idx_analisis_texto_fecha ON analisis_ml_texto(fecha_analisis);
  CREATE INDEX idx_analisis_texto_prioridad ON analisis_ml_texto(prioridad_calculada);
  CREATE INDEX idx_analisis_texto_estado ON analisis_ml_texto(estado_analisis);

  -- 6. ANALISIS_ML_IMAGEN
  CREATE TABLE analisis_ml_imagen (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      multimedia_id UUID NOT NULL REFERENCES multimedia(id) ON DELETE CASCADE,

      -- Análisis de veracidad
      es_imagen_accidente BOOLEAN NOT NULL,
      score_veracidad DECIMAL(5, 4) NOT NULL,
      tipo_escena_detectada VARCHAR(100),

      -- Análisis de gravedad visual
      nivel_gravedad_visual INT CHECK (nivel_gravedad_visual BETWEEN 1 AND 5),
      elementos_criticos_detectados JSONB,

      -- Detección de objetos
      objetos_detectados JSONB,
      personas_detectadas INT DEFAULT 0,
      vehiculos_detectados INT DEFAULT 0,

      -- Clasificación de escena
      categorias_escena JSONB,
      score_confianza_escena DECIMAL(5, 4),

      -- Detección de anomalías (ML no supervisado)
      es_anomalia BOOLEAN,
      score_anomalia DECIMAL(5, 4),
      razon_sospecha TEXT,

      -- Calidad de imagen
      calidad_imagen VARCHAR(20) CHECK (calidad_imagen IN ('excelente', 'buena', 'regular', 'mala')),
      resolucion_imagen VARCHAR(20),
      es_imagen_clara BOOLEAN,

      -- Metadatos del modelo
      modelo_vision VARCHAR(50) DEFAULT 'yolo-v8',
      modelo_veracidad VARCHAR(50) DEFAULT 'autoencoder-v1',
      tiempo_procesamiento_ms INT,

      -- Auditoría
      fecha_analisis TIMESTAMP DEFAULT NOW(),
      estado_analisis VARCHAR(20) DEFAULT 'COMPLETADO'
          CHECK (estado_analisis IN ('PENDIENTE', 'PROCESANDO', 'COMPLETADO', 'ERROR')),
      error_mensaje TEXT
  );

  CREATE INDEX idx_analisis_imagen_multimedia ON analisis_ml_imagen(multimedia_id);
  CREATE INDEX idx_analisis_imagen_veracidad ON analisis_ml_imagen(score_veracidad);
  CREATE INDEX idx_analisis_imagen_gravedad ON analisis_ml_imagen(nivel_gravedad_visual);
  CREATE INDEX idx_analisis_imagen_fecha ON analisis_ml_imagen(fecha_analisis);
  CREATE INDEX idx_analisis_imagen_estado ON analisis_ml_imagen(estado_analisis);

  -- 7. HISTORIAL DE ESTADOS
  CREATE TABLE incidente_historial_estados (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      incidente_id UUID NOT NULL REFERENCES incidente(id) ON DELETE CASCADE,
      estado_anterior VARCHAR(30),
      estado_nuevo VARCHAR(30) NOT NULL,
      usuario_cambio VARCHAR(100) DEFAULT 'SISTEMA',
      motivo TEXT,
      metadata JSONB,
      fecha_cambio TIMESTAMP DEFAULT NOW()
  );

  CREATE INDEX idx_historial_incidente ON incidente_historial_estados(incidente_id);
  CREATE INDEX idx_historial_fecha ON incidente_historial_estados(fecha_cambio);


  --triggers

  CREATE OR REPLACE FUNCTION registrar_cambio_estado()
  RETURNS TRIGGER AS $$
  BEGIN
      IF OLD.estado_incidente IS DISTINCT FROM NEW.estado_incidente THEN
          INSERT INTO incidente_historial_estados (
              incidente_id,
              estado_anterior,
              estado_nuevo,
              usuario_cambio,
              metadata
          ) VALUES (
              NEW.id,
              OLD.estado_incidente,
              NEW.estado_incidente,
              COALESCE(current_setting('app.current_user', TRUE), 'SISTEMA'),
              jsonb_build_object(
                  'prioridad_anterior', OLD.prioridad_final,
                  'prioridad_nueva', NEW.prioridad_final,
                  'veracidad', NEW.score_veracidad
              )
          );
      END IF;
      RETURN NEW;
  END;
  $$ LANGUAGE plpgsql;


    CREATE TRIGGER trg_incidente_estado_cambio
  AFTER UPDATE ON incidente
  FOR EACH ROW
  EXECUTE FUNCTION registrar_cambio_estado();