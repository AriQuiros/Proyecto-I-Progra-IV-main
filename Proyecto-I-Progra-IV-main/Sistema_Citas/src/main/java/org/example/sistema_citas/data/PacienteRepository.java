package org.example.sistema_citas.data;

import org.example.sistema_citas.logic.Paciente;
import org.example.sistema_citas.logic.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    Optional<Paciente> findByUsuario(Usuario usuario);
}

