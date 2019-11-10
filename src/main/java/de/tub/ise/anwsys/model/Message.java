package de.tub.ise.anwsys.model;

import javax.persistence.*;

@Entity
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	long id;
	String content;
	String creator;
	String timestamp;

	@ManyToOne
	Channel channel;

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

}
