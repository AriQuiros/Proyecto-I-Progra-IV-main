package org.example.sistema_citas.dto;

public class HorarioDTO {
    private String diaSemana;
    private String horaInicio;
    private String horaFin;
    private boolean ocupado;
    private String fechaReal;

    public boolean isOcupado() { return ocupado; }

    public void setOcupado(boolean ocupado) { this.ocupado = ocupado; }

    public String getDiaSemana() { return diaSemana; }

    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana;}

    public String getHoraInicio() { return horaInicio;}

    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio;}

    public String getHoraFin() { return horaFin;}

    public void setHoraFin(String horaFin) { this.horaFin = horaFin;}

    public String getFechaReal() { return fechaReal;}

    public void setFechaReal(String fechaReal) { this.fechaReal = fechaReal;}
}