package com.springboot.app.models.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.springboot.app.models.entities.Tag;
import com.springboot.app.models.entities.Tarea;

@Repository
public interface ITareaDao extends JpaRepository<Tarea, String> {

	@NativeQuery(QUERY + "WHERE t.id_guid=?1 AND t.status=1")
	Optional<Tarea> findByIdGuid(String id);

	String QUERY = "SELECT * FROM tareas t ";

	String FILTERS = """
			WHERE (:aplicarPrioridad=false OR t.id_prioridad IN (:prioridadIds) )
			AND (:aplicarTareaStatus=false OR t.id_tarea_status IN (:tareaStatusIds) )
			AND (:desde IS NULL OR DATE(t.fecha_limite) >= :desde )
			AND (:hasta IS NULL OR DATE(t.fecha_limite) <= :hasta )
			AND (:busTitle IS NULL OR MATCH(t.titulo) AGAINST (:busTitle IN NATURAL LANGUAGE MODE))
			AND (:busDesc IS NULL OR MATCH(t.descripcion) AGAINST (:busDesc IN NATURAL LANGUAGE MODE))
			AND (:ownerId IS NULL OR t.owner_id=:ownerId)
			AND t.status=1

			""";

	@NativeQuery(value = QUERY + FILTERS, countQuery = "SELECT COUNT(*) FROM tareas t " + FILTERS)
	Page<Tarea> getAllActives(Pageable pageable, @Param("tareaStatusIds") List<Short> tareaStatusIds,
			@Param("prioridadIds") List<Short> prioridadIds, @Param("desde") LocalDate desde,
			@Param("hasta") LocalDate hasta, @Param("busDesc") String busquedaDesc,
			@Param("busTitle") String busquedaTitulo, @Param("aplicarPrioridad") boolean aplicarPrioridad,
			@Param("aplicarTareaStatus") boolean aplicarTareaStatus, @Param("ownerId") Long ownerId);

	@NativeQuery("""
			SELECT t.* FROM tarea_tags tt INNER JOIN tags t ON tt.id_tag=t.id WHERE tt.id_tarea=?1 AND t.status=1;
			""")
	List<Tag> getTagsFromTarea(String idTarea);

	@NativeQuery("SELECT t.* from tareas t where t.id_guid=?1 and t.owner_id=?2 and t.status=1; ")
	Optional<Tarea> findById(String id, Long userId);

	@NativeQuery("select EXISTS(select * from tareas_usuario tu where tu.usuario_id=?2 and tarea_id=?1) ;")
	int isAsignedToThisTask(String tareaId, Long userId);

	
	
	@NativeQuery(""" 
			
	select
		count(*) tareas_pendientes
	from
		tareas t
	join tarea_status ts on
		ts.id = t.id_tarea_status
	join tareas_usuario tu on
		tu.tarea_id = t.id_guid
	where
		ts.id not in (3, 5)
		and t.status = 1
		and tu.usuario_id = ?1
		and (t.fecha_limite is null
			or t.fecha_limite > NOW())
		
			""")
	int getTareasPendientesCountByUserId(Long userId);
	
	@NativeQuery(""" 
			
	select
		count(*) tareas_vencidas
	from
		tareas_usuario tu
	join tareas t on
		t.id_guid = tu.tarea_id
	join tarea_status ts on
		ts.id = t.id_tarea_status
	where
		tu.usuario_id = ?1
		and t.status = 1
		and t.fecha_limite < now()
		and ts.id != 5
	
			""")
	int getTareasVencidasCountByUserId(Long userId);
	
	@NativeQuery(""" 
			
	select
		count(*) tareas_hoy
	from
		tareas_usuario tu
	join tareas t on
		t.id_guid = tu.tarea_id
	where
		tu.usuario_id = ?1
		and DATE(t.fecha_limite)= DATE(NOW())
		and NOW()< t.fecha_limite 

			""")
	int getTareasHoyCountByUserId(Long userId);
	
	@NativeQuery(""" 
			
	select
		ts.status,
		CAST( SUM(
	    case 
	      when tu.usuario_id = 1 and t.status = 1 then 1
	      else 0
	    end
	  ) as SIGNED) as tareas_usuario
	from
		tarea_status ts
	left join tareas t on
		ts.id = t.id_tarea_status
	left join tareas_usuario tu on
		tu.tarea_id = t.id_guid
	group by
		ts.status
	order by
		ts.status

			""")
	List<Object[]> countTareasByTareaStatus(Long userId);
	
	@NativeQuery(""" 
			
	select
		p.tipo_prioridad prioridad,
		cast( SUM(
	    case 
	      when tu.usuario_id = 1 and t.status = 1 then 1
	      else 0
	    end
	  )
	as SIGNED) as tareas_usuario
	from
		prioridades p
	left join tareas t on
		p.id = t.id_prioridad
	left join tareas_usuario tu on
		tu.tarea_id = t.id_guid
	group by
		p.tipo_prioridad
	order by
		p.tipo_prioridad
		
			""")
	List<Object[]> countTareasByPrioridad(Long userId);

}
