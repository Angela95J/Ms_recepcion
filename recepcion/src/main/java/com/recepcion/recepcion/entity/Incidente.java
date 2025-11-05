package com.recepcion.recepcion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "incidente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Foreign Keys
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Solicitante solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analisis_texto_id")
    private AnalisisMlTexto analisisTexto;

    // Datos del incidente
    @Column(name = "descripcion_original", nullable = false, columnDefinition = "TEXT")
    private String descripcionOriginal;

    @Column(name = "tipo_incidente_reportado", length = 100)
    private String tipoIncidenteReportado;

    @Column(name = "tipo_incidente_clasificado", length = 100)
    private String tipoIncidenteClasificado;

    // Prioridades
    @Column(name = "prioridad_inicial")
    private Integer prioridadInicial;

    @Column(name = "prioridad_texto")
    private Integer prioridadTexto;

    @Column(name = "prioridad_imagen")
    private Integer prioridadImagen;

    @Column(name = "prioridad_final")
    private Integer prioridadFinal;

    // Veracidad
    @Column(name = "score_veracidad", precision = 5, scale = 4)
    private BigDecimal scoreVeracidad;

    @Column(name = "es_verosimil")
    private Boolean esVerosimil;

    // Estado
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_incidente", nullable = false, length = 30)
    private EstadoIncidente estadoIncidente;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    // Auditoría
    @Column(name = "fecha_reporte")
    private LocalDateTime fechaReporte;

    @Column(name = "fecha_analisis_completado")
    private LocalDateTime fechaAnalisisCompletado;

    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Relaciones
    @OneToMany(mappedBy = "incidente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Multimedia> multimedia;

    @OneToMany(mappedBy = "incidente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IncidenteHistorialEstados> historialEstados;

    @PrePersist
    protected void onCreate() {
        fechaReporte = LocalDateTime.now();
        fechaUltimaActualizacion = LocalDateTime.now();
        if (estadoIncidente == null) {
            estadoIncidente = EstadoIncidente.RECIBIDO;
        }
        if (prioridadInicial == null) {
            prioridadInicial = 3;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaUltimaActualizacion = LocalDateTime.now();
    }
}
