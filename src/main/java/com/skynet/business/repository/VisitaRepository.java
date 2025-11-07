package com.skynet.business.repository;

import com.skynet.auth.model.Usuario; // <-- Importa Usuario
import com.skynet.business.model.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- Importa List

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    /**
     * MÉTODO ANTIGUO (Se queda)
     * Para el dashboard personal del Técnico.
     * Busca visitas por el técnico directo y que no estén completadas.
     */
    List<Visita> findByTecnicoAndEstadoNot(Usuario tecnico, String estado);


    // --- ¡MÉTODOS NUEVOS PARA JERARQUÍA! ---

    /**
     * MÉTODO NUEVO (Paso 1.3.A)
     * Para el Dashboard del Supervisor.
     * Busca visitas ACTIVAS por el supervisor ASIGNADO del técnico.
     * Spring Data JPA entiende esta consulta anidada:
     * visita -> tecnico -> supervisorAsignado
     */
    List<Visita> findByTecnico_SupervisorAsignadoAndEstadoNot(Usuario supervisor, String estado);

    /**
     * MÉTODO NUEVO (Paso 1.3.A)
     * Para el Historial del Supervisor.
     * Busca visitas COMPLETADAS por el supervisor ASIGNADO del técnico.
     */
    List<Visita> findByTecnico_SupervisorAsignadoAndEstado(Usuario supervisor, String estado);

    // (El método 'findBySupervisorAndEstado' y 'findBySupervisorAndEstadoNot'
    // han sido eliminados ya que la nueva lógica los reemplaza).
}