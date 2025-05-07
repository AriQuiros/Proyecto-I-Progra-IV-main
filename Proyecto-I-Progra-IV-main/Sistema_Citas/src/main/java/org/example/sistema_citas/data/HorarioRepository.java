package org.example.sistema_citas.data;

import jakarta.transaction.Transactional;
import org.example.sistema_citas.logic.Horario;
import org.example.sistema_citas.logic.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Integer> {
    @Modifying
    @Transactional
    void deleteByDoctor(Medico medico);
}
