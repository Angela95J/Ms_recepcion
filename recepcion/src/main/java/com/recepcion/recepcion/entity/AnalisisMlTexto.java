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
@Table(name = "analisis_ml_texto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalisisMlTexto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "texto_analizado", nullable = false, columnDefinition = "TEXT")
    private String textoAnalizado;

    @Column(name = "prioridad_calculada", nullable = false)
    private Integer prioridadCalculada;

    @Column(name = "nivel_gravedad", nullable = false)
    private Integer nivelGravedad;

    @Column(name = "tipo_incidente_predicho", nullable = false, length = 100)
    private String tipoIncidentePredicho;

    @Type(JsonBinaryType.class)
    @Column(name = "categorias_detectadas", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> categoriasDetectadas;

    @Type(JsonBinaryType.class)
    @Column(name = "palabras_clave_criticas", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> palabrasClaveCriticas;

    @Type(JsonBinaryType.class)
    @Column(name = "entidades_medicas", columnDefinition = "jsonb")
    private Map<String, Object> entidadesMedicas;

    @Column(name = "score_confianza", nullable = false, precision = 5, scale = 4)
    private BigDecimal scoreConfianza;

    @Type(JsonBinaryType.class)
    @Column(name = "probabilidades_categorias", columnDefinition = "jsonb")
    private Map<String, Object> probabilidadesCategorias;

    @Column(name = "modelo_version", length = 50)
    private String modeloVersion;

    @Column(name = "algoritmo_usado", length = 50)
    private String algoritmoUsado;

    @Column(name = "tiempo_procesamiento_ms")
    private Integer tiempoProcesamientoMs;

    @Column(name = "fecha_analisis")
    private LocalDateTime fechaAnalisis;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_analisis", length = 20)
    private EstadoAnalisis estadoAnalisis;

    @Column(name = "error_mensaje", columnDefinition = "TEXT")
    private String errorMensaje;

    // Relación inversa con Incidente (un análisis pertenece a un incidente)
    @OneToOne(mappedBy = "analisisTexto", fetch = FetchType.LAZY)
    private Incidente incidente;

    @PrePersist
    protected void onCreate() {
        fechaAnalisis = LocalDateTime.now();
        if (estadoAnalisis == null) {
            estadoAnalisis = EstadoAnalisis.COMPLETADO;
        }
        if (modeloVersion == null) {
            modeloVersion = "bert-medical-v1.0";
        }
        if (algoritmoUsado == null) {
            algoritmoUsado = "transformer";
        }
    }
}
