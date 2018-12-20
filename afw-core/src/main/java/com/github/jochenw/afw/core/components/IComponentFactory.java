package com.github.jochenw.afw.core.components;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IComponentFactory {
	public static class Key {
		private final @Nonnull Class<? extends Object> type;
		private final @Nonnull String name;
		public Key(@Nonnull Class<? extends Object> pType, String pName) {
			type = pType;
			name = pName;
		}
		@Override
		public int hashCode() {
			return 31 * (31 * 1 + name.hashCode()) + type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Key other = (Key) obj;
			return name.equals(other.name)  &&  type.equals(other.type);
		}
		public Class<? extends Object> getType() {
			return type;
		}
		public String getName() {
			return name;
		}
	}
	@Nullable void initialize(Object pObject);
	@Nullable <O extends Object> Supplier<O> getSupplier(Class<?> pType, String pName);
	@Nullable <O extends Object> O getInstance(Class<?> pType, String pName);
	@Nullable <O extends Object> O getInstance(Class<?> pType);
	@Nonnull <O extends Object> O requireInstance(Class<?> pType, String pName);
	@Nonnull <O extends Object> O requireInstance(Class<?> pType);
	@Nonnull <O extends Object> O newInstance(Class<O> pType);
}
