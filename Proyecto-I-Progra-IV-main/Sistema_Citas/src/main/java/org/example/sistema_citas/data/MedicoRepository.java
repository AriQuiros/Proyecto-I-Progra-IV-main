package org.example.sistema_citas.data;

import org.example.sistema_citas.logic.Medico;
import org.example.sistema_citas.logic.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Integer> {
    List<Medico> findMedicoByEstado(String estado);

    Optional<Medico> findByUsuario(Usuario usuario);

    @Query("SELECT m FROM Medico m LEFT JOIN FETCH m.horarios WHERE m.id = :id")
    Optional<Medico> findByIdFetchHorarios(@Param("id") Integer id);

    List<Medico> findMedicoByEspecialidadContainingIgnoreCaseAndCiudadContainingIgnoreCaseAndEstadoContainingIgnoreCase(String especialidad, String ciudad, String aprobado);

}
