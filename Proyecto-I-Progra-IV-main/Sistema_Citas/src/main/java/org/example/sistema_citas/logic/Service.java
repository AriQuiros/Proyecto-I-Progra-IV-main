package org.example.sistema_citas.logic;

import org.example.sistema_citas.data.*;
import org.example.sistema_citas.dto.HorarioDTO;
import org.example.sistema_citas.dto.MedicoCardDTO;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.example.sistema_citas.dto.PacienteDTO;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@org.springframework.stereotype.Service

public class Service {

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private HorarioRepository horarioRepository;
    @Autowired
    private MedicoRepository medicoRepository;
    @Autowired
    private PacienteRepository pacienteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    //usuarios
    public boolean existeUsuarioConNombre(String nombre) {
        return usuarioRepository.findFirstByNombre(nombre).isPresent();
    }

    //Citas
    public Iterable<Cita> findAllCitas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> findCitaById(Integer id) {
        return citaRepository.findById(id);
    }

    public Cita saveCita(Cita cita) {
        return citaRepository.save(cita);
    }

    public void deleteCitaById(Integer id) {
        citaRepository.deleteById(id);
    }

    public List<Cita> buscarCitasPorMedico(Medico medico, String estado, String paciente) {
        if (medico == null) return Collections.emptyList();

        if (estado != null && estado.isBlank()) estado = null;
        if (paciente != null && paciente.isBlank()) paciente = null;

        if (estado == null && paciente == null) {
            return citaRepository.findByDoctor(medico);
        } else if (estado != null && paciente == null) {
            return citaRepository.findByDoctorAndEstado(medico, estado);
        } else if (estado == null) {
            return citaRepository.findByDoctorAndPacienteUsuarioNombreContainingIgnoreCase(medico, paciente);
        } else {
            return citaRepository.findByDoctorAndEstadoAndPacienteUsuarioNombreContainingIgnoreCase(medico, estado, paciente);
        }
    }

    public List<Cita> buscarCitasPorPaciente(Paciente paciente, String estado, String medico) {
        if (estado != null && estado.isBlank()) estado = null;
        if (medico != null && medico.isBlank()) medico = null;
        if (estado == null && medico == null) {
            return citaRepository.findByPacienteOrderByFechaHoraDesc(paciente);
        } else if (estado != null && medico == null) {
            return citaRepository.findByPacienteAndEstadoOrderByFechaHoraDesc(paciente, estado);
        } else if (estado == null) {
            return citaRepository.findByPacienteAndDoctorUsuarioNombreContainingIgnoreCaseOrderByFechaHoraDesc(paciente, medico);
        } else {
            return citaRepository.findByPacienteAndEstadoAndDoctorUsuarioNombreContainingIgnoreCaseOrderByFechaHoraDesc(paciente, estado, medico);
        }
    }

    //Horarios
    public Iterable<Horario> findAllHorarios() {
        return horarioRepository.findAll();
    }

    public Optional<Horario> findHorarioById(Integer id) {
        return horarioRepository.findById(id);
    }

    public Horario saveHorario(Horario horario) {
        return horarioRepository.save(horario);
    }

    public void deleteHorarioById(Integer id) {
        horarioRepository.deleteById(id);
    }


    //Medico
    public Medico findMedicoByIdConHorarios(Integer id) { return medicoRepository.findByIdFetchHorarios(id).orElse(null);}

    public void eliminarHorariosByDoctor(Medico medico) {
        horarioRepository.deleteByDoctor(medico);
    }

    public Optional<Medico> findMedicoByUsuario(Usuario usuario) {
        return medicoRepository.findByUsuario(usuario);
    }

    public Iterable<Medico> findAllMedicos() {
        return medicoRepository.findAll();
    }

    public List<Medico> findMedicoByEspecialidadContainingIgnoreCaseAndCiudadContainingIgnoreCaseAndEstadoContainingIgnoreCase(String especialidad, String ciudad,String estado) {
        return medicoRepository.findMedicoByEspecialidadContainingIgnoreCaseAndCiudadContainingIgnoreCaseAndEstadoContainingIgnoreCase(especialidad, ciudad, estado);
    }

    public Iterable<Medico> findAllMedicoAprobados(){
        List<Medico> medicos = new ArrayList<>();
        for(Medico medico : medicoRepository.findAll()){
            if(Objects.equals(medico.getEstado(), "APROBADO"))
                medicos.add(medico);
        }
        return medicos;
    }

    public Optional<Medico> findMedicoById(Integer id) {
        Medico medico = medicoRepository.findById(id).orElse(null);
        if (medico != null) {
            Hibernate.initialize(medico.getUsuario());
        }
        return Optional.ofNullable(medico);
    }

    public Medico saveMedico(Medico medico) {
        saveUsuario(medico.getUsuario());
        return medicoRepository.save(medico);
    }

    public void deleteMedicoById(Integer id) {
        medicoRepository.deleteById(id);
    }

    public List<MedicoCardDTO> buscarMedicos(String especialidad, String ciudad) {
        if (especialidad == null) especialidad = "";
        if (ciudad == null) ciudad = "";

        List<Medico> medicosFiltrados = medicoRepository
                .findMedicoByEspecialidadContainingIgnoreCaseAndCiudadContainingIgnoreCaseAndEstadoContainingIgnoreCase(especialidad, ciudad, "APROBADO");

        return convertirAMedicoCardDTO(medicosFiltrados);
    }

    public List<MedicoCardDTO> convertirAMedicoCardDTO(List<Medico> medicos) {
        List<MedicoCardDTO> result = new ArrayList<>();
        for (Medico medico : medicos) {
            MedicoCardDTO dto = new MedicoCardDTO();
            dto.setId(medico.getId());
            dto.setNombre(medico.getUsuario().getNombre());
            dto.setEspecialidad(medico.getEspecialidad());
            dto.setCiudad(medico.getCiudad());
            dto.setInstalacion(medico.getInstalacion());
            dto.setCostoConsulta(medico.getCostoConsulta());
            dto.setImagen(medico.getImagen());

            List<HorarioDTO> horarios = new ArrayList<>();
            for (Horario h : medico.getHorarios()) {
                HorarioDTO horarioDTO = new HorarioDTO();
                horarioDTO.setDiaSemana(convertirDia(h.getDia()));
                horarioDTO.setHoraInicio(h.getHorainicio() + ":00");
                horarioDTO.setHoraFin(h.getHorafin() + ":00");

                LocalDate today = LocalDate.now();
                DayOfWeek diaSemana = DayOfWeek.of(h.getDia());
                LocalDate targetDate = today.with(TemporalAdjusters.nextOrSame(diaSemana));
                horarioDTO.setFechaReal(targetDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                boolean ocupado = citaRepository.existsByDoctorAndFechaHoraBetween(
                        medico,
                        targetDate.atTime(h.getHorainicio(), 0),
                        targetDate.atTime(h.getHorafin(), 0)
                );
                horarioDTO.setOcupado(ocupado);

                horarios.add(horarioDTO);
            }

            dto.setHorarios(horarios);
            result.add(dto);
        }
        return result;
    }

    public MedicoCardDTO convertirAMedicoPerfilDTO(Medico medico) {
        MedicoCardDTO dto = new MedicoCardDTO();
        dto.setNombre(medico.getUsuario().getNombre());
        dto.setId(medico.getId());
        dto.setEspecialidad(medico.getEspecialidad());
        dto.setCiudad(medico.getCiudad());
        dto.setInstalacion(medico.getInstalacion());
        dto.setCostoConsulta(medico.getCostoConsulta());
        dto.setFrecuencia(medico.getFrecuencia());
        dto.setImagen(medico.getImagen());

        Map<Integer, Horario> horariosPorDia = new HashMap<>();
        for (Horario h : medico.getHorarios()) {
            horariosPorDia.put(h.getDia(), h);
        }

        List<HorarioDTO> listaHorarios = new ArrayList<>();
        List<String> diasSemana = List.of("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");

        for (int i = 0; i < diasSemana.size(); i++) {
            String nombreDia = diasSemana.get(i);
            int numeroDia = i + 1;

            HorarioDTO horarioDTO = new HorarioDTO();
            horarioDTO.setDiaSemana(nombreDia);

            Horario h = horariosPorDia.get(numeroDia);
            if (h != null) {
                horarioDTO.setHoraInicio(String.format("%02d:00", h.getHorainicio()));
                horarioDTO.setHoraFin(String.format("%02d:00", h.getHorafin()));

                LocalDate today = LocalDate.now();
                DayOfWeek diaSemana = DayOfWeek.of(h.getDia());
                LocalDate targetDate = today.with(TemporalAdjusters.nextOrSame(diaSemana));

                String fechaFormateada = targetDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                horarioDTO.setFechaReal(fechaFormateada);

                boolean ocupado = citaRepository.existsByDoctorAndFechaHoraBetween(
                        medico,
                        targetDate.atTime(h.getHorainicio(), 0),
                        targetDate.atTime(h.getHorafin(), 0)
                );
                horarioDTO.setOcupado(ocupado);
            } else {
                horarioDTO.setHoraInicio("");
                horarioDTO.setHoraFin("");
                horarioDTO.setFechaReal("");
                horarioDTO.setOcupado(false);
            }
            listaHorarios.add(horarioDTO);
        }
        dto.setHorarios(listaHorarios);
        return dto;
    }

    public List<Medico> findMedicosByEstado(String pendiente) {
        System.out.println(medicoRepository.findMedicoByEstado(pendiente).toArray().toString());
        return medicoRepository.findMedicoByEstado(pendiente);
    }

    public List<MedicoCardDTO> getMedicosConHorarios() {
        List<MedicoCardDTO> result = new ArrayList<>();
        Iterable<Medico> medicos = medicoRepository.findMedicoByEstado("APROBADO");

        for (Medico medico : medicos) {
            MedicoCardDTO dto = new MedicoCardDTO();
            dto.setId(medico.getId());
            dto.setNombre(medico.getUsuario().getNombre());
            dto.setEspecialidad(medico.getEspecialidad());
            dto.setCiudad(medico.getCiudad());
            dto.setInstalacion(medico.getInstalacion());
            dto.setCostoConsulta(medico.getCostoConsulta());
            dto.setImagen(medico.getImagen());

            List<HorarioDTO> horarios = new ArrayList<>();
            for (Horario h : medico.getHorarios()) {
                HorarioDTO horarioDTO = new HorarioDTO();
                horarioDTO.setDiaSemana(convertirDia(h.getDia()));
                horarioDTO.setHoraInicio(h.getHorainicio() + ":00");
                horarioDTO.setHoraFin(h.getHorafin() + ":00");

                // Calcular fecha real
                LocalDate today = LocalDate.now();
                DayOfWeek diaSemana = DayOfWeek.of(h.getDia());
                LocalDate targetDate = today.with(TemporalAdjusters.nextOrSame(diaSemana));
                horarioDTO.setFechaReal(targetDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                // Verificar si la franja está ocupada
                boolean ocupado = citaRepository.existsByDoctorAndFechaHoraBetween(
                        medico,
                        targetDate.atTime(h.getHorainicio(), 0),
                        targetDate.atTime(h.getHorafin(), 0)
                );
                horarioDTO.setOcupado(ocupado);

                horarios.add(horarioDTO);
            }

            dto.setHorarios(horarios);
            result.add(dto);
        }
        return result;
    }


    //Paciente
    public Iterable<Paciente> findAllPacientes() {
        return pacienteRepository.findAll();
    }

    public Optional<Paciente> findPacienteById(Integer id) {
        return pacienteRepository.findById(id);
    }

    public void deletePacienteById(Integer id) {
        pacienteRepository.deleteById(id);
    }

    public Optional<Paciente> findPacienteByUsuario(Usuario usuario) { return pacienteRepository.findByUsuario(usuario);}

    public Optional<PacienteDTO> findPacienteDTOById(Integer id) {
        return pacienteRepository.findById(id)
                .map(PacienteDTO::new);
    }

    public Paciente savePaciente(Paciente paciente) {
        saveUsuario(paciente.getUsuario());
        return pacienteRepository.save(paciente);
    }

    //Usuarios
    public Iterable<Usuario> findAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findUsuarioById(Integer id) {
        return usuarioRepository.findById(id);
    }

    public void deleteUsuarioById(Integer id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario findUsuarioByNombreAndClave(String nombre, String clave) {
        return usuarioRepository.findFirstByNombreAndClave(nombre, clave);
    }

    public Usuario saveUsuario(Usuario usuario) {
        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }
    }

    //extra
    private String convertirDia(int dia) {
        return switch (dia) {
            case 1 -> "Lunes";
            case 2 -> "Martes";
            case 3 -> "Miércoles";
            case 4 -> "Jueves";
            case 5 -> "Viernes";
            case 6 -> "Sábado";
            case 7 -> "Domingo";
            default -> "Desconocido";
        };
    }

}