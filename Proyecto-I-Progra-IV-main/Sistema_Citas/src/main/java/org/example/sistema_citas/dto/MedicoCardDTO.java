package org.example.sistema_citas.dto;

import java.util.List;

public class MedicoCardDTO {
    private Integer id;
    private String nombre;
    private String especialidad;
    private String ciudad;
    private String instalacion;
    private List<HorarioDTO> horarios;
    private Integer costoConsulta;
    private Integer frecuencia;
    private String imagen;

    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecialidad() { return especialidad; }

    public void setEspecialidad(String especialidad) { this.especialidad = especialidad;}

    public String getCiudad() { return ciudad;}

    public void setCiudad(String ciudad) { this.ciudad = ciudad;}

    public String getInstalacion() { return instalacion;}

    public void setInstalacion(String instalacion) { this.instalacion = instalacion;}

    public List<HorarioDTO> getHorarios() {return horarios;}

    public void setHorarios(List<HorarioDTO> horarios) {this.horarios = horarios;}

    public Integer getCostoConsulta() { return costoConsulta;}

    public void setCostoConsulta(Integer costoConsulta) { this.costoConsulta = costoConsulta;}

    public Integer getFrecuencia() { return frecuencia;}

    public void setFrecuencia(Integer frecuencia) { this.frecuencia = frecuencia;}

    public String getImagen() {return imagen;
    }

    public void setImagen(String imagen) {this.imagen = imagen;}
}
