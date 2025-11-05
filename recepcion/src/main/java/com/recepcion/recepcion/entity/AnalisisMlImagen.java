package com.recepcion.recepcion.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "analisis_ml_imagen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalisisMlImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multimedia_id", nullable = false)
    private Multimedia multimedia;

    // Análisis de veracidad
    @Column(name = "es_imagen_accidente", nullable = false)
    private Boolean esImagenAccidente;

    @Column(name = "score_veracidad", nullable = false, precision = 5, scale = 4)
    private BigDecimal scoreVeracidad;

    @Column(name = "tipo_escena_detectada", length = 100)
    private String tipoEscenaDetectada;

    // Análisis de gravedad visual
    @Column(name = "nivel_gravedad_visual")
    private Integer nivelGravedadVisual;

    @Type(JsonBinaryType.class)
    @Column(name = "elementos_criticos_detectados", columnDefinition = "jsonb")
    private Map<String, Object> elementosCriticosDetectados;

    // Detección de objetos
    @Type(JsonBinaryType.class)
    @Column(name = "objetos_detectados", columnDefinition = "jsonb")
    private Map<String, Object> objetosDetectados;

    @Column(name = "personas_detectadas")
    private Integer personasDetectadas;

    @Column(name = "vehiculos_detectados")
    private Integer vehiculosDetectados;

    // Clasificación de escena
    @Type(JsonBinaryType.class)
    @Column(name = "categorias_escena", columnDefinition = "jsonb")
    private Map<String, Object> categoriasEscena;

    @Column(name = "score_confianza_escena", precision = 5, scale = 4)
    private BigDecimal scoreConfianzaEscena;

    // Detección de anomalías (ML no supervisado)
    @Column(name = "es_anomalia")
    private Boolean esAnomalia;

    @Column(name = "score_anomalia", precision = 5, scale = 4)
    private BigDecimal scoreAnomalia;

    @Column(name = "razon_sospecha", columnDefinition = "TEXT")
    private String razonSospecha;

    // Calidad de imagen
    @Enumerated(EnumType.STRING)
    @Column(name = "calidad_imagen", length = 20)
    private CalidadImagen calidadImagen;

    @Column(name = "resolucion_imagen", length = 20)
    private String resolucionImagen;

    @Column(name = "es_imagen_clara")
    private Boolean esImagenClara;

    // Metadatos del modelo
    @Column(name = "modelo_vision", length = 50)
    private String modeloVision;

    @Column(name = "modelo_veracidad", length = 50)
    private String modeloVeracidad;

    @Column(name = "tiempo_procesamiento_ms")
    private Integer tiempoProcesamientoMs;

    // Auditoría
    @Column(name = "fecha_analisis")
    private LocalDateTime fechaAnalisis;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_analisis", length = 20)
    private EstadoAnalisis estadoAnalisis;

    @Column(name = "error_mensaje", columnDefinition = "TEXT")
    private String errorMensaje;

    @PrePersist
    protected void onCreate() {
        fechaAnalisis = LocalDateTime.now();
        if (estadoAnalisis == null) {
            estadoAnalisis = EstadoAnalisis.COMPLETADO;
        }
        if (modeloVision == null) {
            modeloVision = "yolo-v8";
        }
        if (modeloVeracidad == null) {
            modeloVeracidad = "autoencoder-v1";
        }
        if (personasDetectadas == null) {
            personasDetectadas = 0;
        }
        if (vehiculosDetectados == null) {
            vehiculosDetectados = 0;
        }
    }
}
