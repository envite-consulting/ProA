package de.envite.process.map.repository.tables;

import java.util.List;
import java.util.stream.Collectors;

import de.envite.process.map.entities.ProcessInformation;
import de.envite.process.map.usecases.ProcesModelRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class JpaProcessmodelRepository implements ProcesModelRepository {
	
	@Inject
	private EntityManager em;

	@Override
	@Transactional
	public Long saveProcessModel(String name, String xml) {
		ProcessModel model = new ProcessModel();
		model.setBpmnXml(xml);
		model.setName(name);
		
		em.persist(model);
		em.flush();
		return model.getId();
	}

	@Override
	@Transactional
	public String getProcessModel(Long id) {
		
		return em.find(ProcessModel.class, id).getBpmnXml();
	}

	@Override
	@Transactional
	public List<ProcessInformation> getProcessModels() {
		return em//
				.createQuery("SELECT pm FROM ProcessModel pm", ProcessModel.class)//
				.getResultList()//
				.stream()//
				.map(model -> new ProcessInformation(model.getId(), model.getName()))//
				.collect(Collectors.toList());
	}
}