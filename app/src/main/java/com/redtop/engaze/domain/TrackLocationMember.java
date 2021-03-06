package com.redtop.engaze.domain;

import com.redtop.engaze.common.enums.AcceptanceStatus;

public class TrackLocationMember {
	private Event event;
	private EventParticipant member;
	private AcceptanceStatus acceptance;


	public TrackLocationMember(Event event, EventParticipant member, AcceptanceStatus acceptance) {
		super();
		this.event = event;
		this.member = member;
		this.acceptance = acceptance;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public void setMember(EventParticipant member) {
		this.member = member;
	}

	public EventParticipant getMember() {
		return this.member;
	}
	
	public void setAcceptance(AcceptanceStatus acceptance) {
		this.acceptance = acceptance;
	}

	public AcceptanceStatus getAcceptance() {
		return this.acceptance;
	}

}