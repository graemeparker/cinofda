package com.adfonic.dto.publisher;

import com.adfonic.dto.NameIdBusinessDto;

public class PublisherDto extends NameIdBusinessDto {

	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PublisherDto [name=");
		builder.append(name);
		builder.append(", ");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

}
