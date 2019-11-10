
package de.tub.ise.anwsys.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.tub.ise.anwsys.model.Message;

public interface MessageRepository extends CrudRepository<Message, Long> {

	List<Message> findByChannelId(long id);

	List<Message> findByChannelIdOrderByTimestampDesc(long id);

}
