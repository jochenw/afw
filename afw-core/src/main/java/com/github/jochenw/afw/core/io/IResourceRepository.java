package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;


/**
 * A resource repository is a set of resources. Examples are:
 * <ul>
 *   <li>A directory, and the files contained therein. This is implemented by the
 *     {@link DirResourceRepository}.</li>
 *   <li>A zip file, and the files contained therein. This is implemented by the
 *     {@link ZipFileResourceRepository}.</li>
 *   <li>A {@link ClassLoader class loader}, and the files, which are accessible
 *     through the class loader. The associated implementation is the
 *     {@link ClassLoaderResourceRepository}.</li>
 * </ul>
 */
public interface IResourceRepository {
	/** Interface of a resource. Resources are being obtained by invoking
	 * {@link IResourceRepository#list(Consumer)}.
	 */
	public interface IResource {
		/** Returns the resources namespace. This is basically a Java package name. However,
		 * the components of the name aren't necessarily compliant to Java conventions.
		 * @return The resources namespace.
		 */
		public String getNamespace();
		/** Returns the resources URI. A URI is intended to be a human readable description of
		 * the resources location. Typically, this will include the namespace.
		 * @return A URI, which indicates the resources location.
		 */
		public String getUri();
		/** If the resource is mutable: Returns an immutable representation of this resource.
		 * Otherwise, returns the resource itself.
		 * @return An immutable representation of this resource.
		 */
		public IResource makeImmutable();
	}

	/**
	 * Iterates over all the resources in the repository. Do not assume a particular order.
	 * The order depends on the repository implementation, and/or on the repository.
	 * @param pConsumer A resource consumer, which is being invoked for the resources, one by one.
	 */
	public void list(Consumer<IResource> pConsumer);
	/** Returns an {@link InputStream} for reading the resource. No guarantee is made,
	 * that more than one such stream can exist simultaneously. This depends on the
	 * repository implementation, perhaps even on the repository itself.
	 * @param pResource The resource to open. Must have been created by this repository.
	 * @return An opened {@link InputStream}. The caller is responsible for closing the stream.
	 * @throws IOException Accessing the resource failed.
	 */
	public InputStream open(IResource pResource) throws IOException;
}
