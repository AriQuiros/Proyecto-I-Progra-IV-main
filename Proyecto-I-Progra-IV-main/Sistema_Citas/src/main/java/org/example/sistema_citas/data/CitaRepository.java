package org.example.sistema_citas.data;

import org.example.sistema_citas.logic.Cita;
import org.example.sistema_citas.logic.Medico;
import org.example.sistema_citas.logic.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByDoctor(Medico doctor);

    List<Cita> findByDoctorAndEstado(Medico doctor, String estado);

    List<Cita> findByDoctorAndPacienteUsuarioNombreContainingIgnoreCase(Medico doctor, String nombre);

    List<Cita> findByDoctorAndEstadoAndPacienteUsuarioNombreContainingIgnoreCase(Medico doctor, String estado, String nombre);

    boolean existsByDoctorAndFechaHoraBetween(Medico doctor, LocalDateTime inicio, LocalDateTime fin);

    List<Cita> findByPacienteOrderByFechaHoraDesc(Paciente paciente);

    List<Cita> findByPacienteAndEstadoOrderByFechaHoraDesc(Paciente paciente, String estado);

    List<Cita> findByPacienteAndDoctorUsuarioNombreContainingIgnoreCaseOrderByFechaHoraDesc(Paciente paciente, String nombre);

    List<Cita> findByPacienteAndEstadoAndDoctorUsuarioNombreContainingIgnoreCaseOrderByFechaHoraDesc(Paciente paciente, String estado, String nombre);

}
