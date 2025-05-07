package org.example.sistema_citas.data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.example.sistema_citas.logic.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Usuario findFirstByNombreAndClave(String nombre, String clave);

    Optional<Usuario> findFirstByNombre(String nombre);

}
