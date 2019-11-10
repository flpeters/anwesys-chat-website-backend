
package de.tub.ise.anwsys.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resources;
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
@RequestMapping(path = "/channels/{id}/messages")
public class MessageController {

    @Autowired
    private Environment env;

    MessageRepository messageRepository;
    ChannelRepository channelRepository;
    SimpleDateFormat dateFormat;

    @Autowired
    public MessageController(MessageRepository messageRepository,
                             ChannelRepository channelRepository) {
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createMessage(@PathVariable long id, @RequestBody Message message,
                                           @RequestParam(name = "timestamp", required = false) String lastSeenTimestamp,
                                           @RequestHeader("X-Group-Token") String token) throws UnsupportedEncodingException {
        if (isAuthenticated(token)) {
            /* Send a message to a specific channel */
            Optional<Channel> channel = channelRepository.findById(id);
            if (channel.isPresent()) {
                Message m = new Message();
                m.setChannel(channel.get());
                m.setCreator(message.getCreator());
                m.setContent(message.getContent());
                m.setTimestamp(this.dateFormat.format(new Date()));
                messageRepository.save(m);
                if (lastSeenTimestamp != null) {
                    List<Message> messages = messageRepository.findByChannelIdOrderByTimestampDesc(id);
                    List<Message> mList = new ArrayList<>();
                    try {
                        Timestamp lastSeenTS = convertTimestamp(lastSeenTimestamp);
                        int counter = 0;
                        for (Message messageChannel : messages) {
                            Timestamp messageTS = convertTimestamp(messageChannel.getTimestamp());
                            if (messageTS.after(lastSeenTS) || messageTS.equals(lastSeenTS)) {
                                mList.add(message);
                                counter += 1;
                            }
                            if (counter >= 50) {
                                break;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    PagedResources.PageMetadata pagedResources =
                            new PagedResources.PageMetadata(mList.size(), 0, messages.size(), 1);
                    return ResponseEntity.ok(new PagedResources<>(mList, pagedResources,
                            new Link("/channels/" + id)));
                } else {
                    PagedResources.PageMetadata pagedResources =
                            new PagedResources.PageMetadata(1, 0, 1, 1);
                    return ResponseEntity.ok(new PagedResources<>(Arrays.asList(m), pagedResources,
                            new Link("/channels/" + id)));
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private Timestamp convertTimestamp(String timestamp) throws ParseException, UnsupportedEncodingException {
        String decodedTimestamp = URLDecoder.decode(timestamp, "UTF-8");
        return new Timestamp(this.dateFormat.parse(decodedTimestamp).getTime());
    }

    private boolean isAuthenticated(String token) {
        List<String> serverTokens = Arrays.asList(env.getProperty("X-Group-Token").split(","));
        return serverTokens.contains(token);
    }

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Resources<Message>> getMessagesOfChannel(@PathVariable long id,
                                                                   @RequestParam(name = "lastSeenTimestamp", required = false) String lastSeenTimestamp,
                                                                   @RequestHeader("X-Group-Token") String token) throws UnsupportedEncodingException {
        if (isAuthenticated(token)) {
            /* Get the most recent messages of a specific channel */
            if (channelRepository.findById(id).isPresent()) {
                List<Message> messages = messageRepository.findByChannelIdOrderByTimestampDesc(id);
                List<Message> mList = new ArrayList<>();
                if (!messages.isEmpty()) {
                    if (lastSeenTimestamp == null) {
                        mList = messages.subList(0, Math.min(10, messages.size()));
                    } else {
                        try {
                            Timestamp lastSeenTS = convertTimestamp(lastSeenTimestamp);
                            int counter = 0;
                            for (Message message : messages) {
                                Timestamp messageTS = convertTimestamp(message.getTimestamp());
                                if (messageTS.after(lastSeenTS) || messageTS.equals(lastSeenTS)) {
                                    mList.add(message);
                                    counter += 1;
                                }
                                if (counter >= 50) {
                                    break;
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                int size = mList.size();
                int totalSize = messages.size();
                int totalPages = 1;
                if (size > 0) {
                    totalPages = ((totalSize - 1) / size + 1);
                }
                PagedResources.PageMetadata pagedResources =
                        new PagedResources.PageMetadata(size, 0, totalSize, totalPages);
                return ResponseEntity.ok(new PagedResources<>(mList, pagedResources,
                        new Link("/channels/" + id)));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
