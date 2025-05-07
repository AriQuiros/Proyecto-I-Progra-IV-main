package org.example.sistema_citas.logic;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "medico")
public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id", nullable = false)
    private Usuario usuario;

    @Size(max = 100)
    @NotNull
    @Column(name = "especialidad", nullable = false, length = 100)
    private String especialidad;

    @Size(max = 100)
    @NotNull
    @Column(name = "ciudad", nullable = false, length = 100)
    private String ciudad;

    @NotNull
    @Column(name = "costo_consulta", nullable = false)
    private Integer costoConsulta;

    @NotNull
    @Column(name = "frecuencia", nullable = false)
    private Integer frecuencia;

    @Size(max = 100)
    @NotNull
    @Column(name = "instalacion", nullable = false, length = 100)
    private String instalacion;

    @NotNull
    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // Nuevo campo: PENDIENTE, APROBADO, RECHAZADO

    @Size(max = 255)
    @Column(name = "imagen", length = 255)
    private String imagen;

    @OneToMany(mappedBy = "doctor")
    private Set<Cita> citas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Horario> horarios = new LinkedHashSet<>();

    // Getters y setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Integer getCostoConsulta() {
        return costoConsulta;
    }

    public void setCostoConsulta(Integer costoConsulta) {
        this.costoConsulta = costoConsulta;
    }

    public Integer getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(Integer frecuencia) {
        this.frecuencia = frecuencia;
    }

    public String getInstalacion() {
        return instalacion;
    }

    public void setInstalacion(String instalacion) {
        this.instalacion = instalacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Set<Cita> getCitas() {
        return citas;
    }

    public void setCitas(Set<Cita> citas) {
        this.citas = citas;
    }

    public Set<Horario> getHorarios() {
        return horarios;
    }

    public void setHorarios(Set<Horario> horarios) {
        this.horarios = horarios;
    }

    public String getImagen() { return imagen;}

    public void setImagen(String imagen) { this.imagen = imagen;}
}