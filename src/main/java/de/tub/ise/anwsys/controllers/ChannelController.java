package de.tub.ise.anwsys.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.tub.ise.anwsys.model.Channel;
import de.tub.ise.anwsys.model.Message;
import de.tub.ise.anwsys.repositories.ChannelRepository;
import de.tub.ise.anwsys.repositories.MessageRepository;

@RestController
@RequestMapping(path = "/channels")
public class ChannelController {

    @Autowired
    private Environment env;

    ChannelRepository channelRepository;
    MessageRepository messageRepository;
    SimpleDateFormat dateFormat;

    @Autowired
    public ChannelController(ChannelRepository channelRepository,
                             MessageRepository messageRepository) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createChannel(
            @RequestBody Channel channel,
            @RequestHeader("X-Group-Token") String token) {
        if (isAuthenticated(token)) {
            /* Create a new channel */
            if (channelRepository.existsByName(channel.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else {
                Channel c = new Channel();
                c.setName(channel.getName());
                c.setTopic(channel.getTopic());
                channelRepository.save(c);
                HttpHeaders responseHeaders = new HttpHeaders();
                URI location = null;
                try {
                    location = new URI("/channels/" + c.getId());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                responseHeaders.setLocation(location);
                return ResponseEntity.status(HttpStatus.CREATED).headers(responseHeaders).body(c);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Resources<Channel>> getAllChannels(
            @RequestHeader("X-Group-Token") String token,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "20") int size) {
        if (isAuthenticated(token)) {
            /* Get a list of channels */
            if (size <= 0) {
                size = 20;
            }
            if (page <= 0) {
                page = 0;
            }
            Collection<Channel> findAll = channelRepository.findAll();
            int totalSize = findAll.size();
            int from = (page * size) + 1;
            int to = (page + 1) * size;
            findAll.removeIf(c -> (c.getId() < from || c.getId() > to));
            PagedResources.PageMetadata pagedResources =
                    new PagedResources.PageMetadata(size, page, totalSize,
                            ((totalSize - 1) / size + 1));
            Resources<Channel> resources = new PagedResources<>(findAll,
                    pagedResources, new Link("/channels"));
            return ResponseEntity.ok(resources);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Channel> getChannelByID(
            @PathVariable long id,
            @RequestHeader("X-Group-Token") String token) {
        if (isAuthenticated(token)) {
            /* Get information on one specific channel */
            Optional<Channel> findById = channelRepository.findById(id);
            if (findById.isPresent()) {
                return ResponseEntity.ok(findById.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private Timestamp convertTimestamp(String timestamp) throws ParseException {
        return new Timestamp(this.dateFormat.parse(timestamp).getTime());
    }

    private boolean isAuthenticated(String token) {
        List<String> serverTokens = Arrays.asList(env.getProperty("X-Group-Token").split(","));
        return serverTokens.contains(token);
    }

    @RequestMapping(path = "/{id}/users", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Object[]> getUsersByChannelID(
            @PathVariable long id,
            @RequestHeader("X-Group-Token") String token) {
        if (isAuthenticated(token)) {
            /* Get the list of users for a specific channel */
            if (channelRepository.findById(id).isPresent()) {
                List<Message> messages = messageRepository.findByChannelIdOrderByTimestampDesc(id);
                HashSet<String> creatorList = new HashSet<>();
                long currentTS = new Timestamp(System.currentTimeMillis()).getTime();
                for (Message message : messages) {
                    try {
                        long messageTS = convertTimestamp(message.getTimestamp()).getTime();
                        int minutes = (int) (currentTS - messageTS) / 60000;
                        if (minutes < 10) {
                            creatorList.add(message.getCreator());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                return ResponseEntity.ok(creatorList.toArray());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
