package com.github.jochenw.afw.db.api;

import java.util.Objects;

public class DefaultId implements IId {
	private static final long serialVersionUID = -6291169162564291215L;

	private final String id;

	public DefaultId(String pId) {
		Objects.requireNonNull(pId, "id");
		id = pId;
	}
	
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return 31 + id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultId other = (DefaultId) obj;
		return id.equals(other.id);
	}
}
