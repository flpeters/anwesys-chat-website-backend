package de.tub.ise.anwsys.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.tub.ise.anwsys.model.Channel;

public interface ChannelRepository extends CrudRepository<Channel, Long> {

	Collection<Channel> findAll();

	List<Channel> findByName(String name);

	boolean existsByName(String name);

}
