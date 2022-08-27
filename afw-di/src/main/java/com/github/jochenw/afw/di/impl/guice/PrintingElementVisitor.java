package com.github.jochenw.afw.di.impl.guice;

import java.io.OutputStream;
import java.io.PrintStream;

import com.google.inject.Binding;
import com.google.inject.spi.DisableCircularProxiesOption;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementVisitor;
import com.google.inject.spi.Elements;
import com.google.inject.spi.InjectionRequest;
import com.google.inject.spi.InterceptorBinding;
import com.google.inject.spi.MembersInjectorLookup;
import com.google.inject.spi.Message;
import com.google.inject.spi.ModuleAnnotatedMethodScannerBinding;
import com.google.inject.spi.PrivateElements;
import com.google.inject.spi.ProviderLookup;
import com.google.inject.spi.ProvisionListenerBinding;
import com.google.inject.spi.RequireAtInjectOnConstructorsOption;
import com.google.inject.spi.RequireExactBindingAnnotationsOption;
import com.google.inject.spi.RequireExplicitBindingsOption;
import com.google.inject.spi.ScopeBinding;
import com.google.inject.spi.StaticInjectionRequest;
import com.google.inject.spi.TypeConverterBinding;
import com.google.inject.spi.TypeListenerBinding;

/** An implementation of {@link ElementVisitor}, that prints its
 * invocation to an output stream, thereby visualizing the registration
 * of bindings.
 * 
 * A debuggging tool for developers.
 */
@SuppressWarnings("rawtypes")
public class PrintingElementVisitor implements ElementVisitor {
	private final PrintStream out;

	/** Creates a new instance, that writes debugging information to the
	 * given output stream.
	 * @param pOut The output stream.
	 */
	public PrintingElementVisitor(OutputStream pOut) {
		if (pOut instanceof PrintStream) {
			out = (PrintStream) pOut;
		} else {
			out = new PrintStream(pOut);
		}
	}

	@Override
	public Object visit(Binding pBinding) {
		out.println("Binding: " + pBinding.getKey() + ", "
				+ pBinding.getSource() + ", ");
		return null;
	}

	@Override
	public Object visit(InterceptorBinding pBinding) {
		out.println("Interceptorinding: "
				+ pBinding.getSource());
		return null;
	}

	@Override
	public Object visit(ScopeBinding pBinding) {
		out.println("ScopeBinding: "
				+ pBinding.getScope() + ", " + pBinding.getSource());
		return null;
	}

	@Override
	public Object visit(TypeConverterBinding pBinding) {
		out.println("TypeConverterBinding: "
				+ pBinding.getTypeConverter() + ", " + pBinding.getSource());
		return null;
	}

	@Override
	public Object visit(InjectionRequest pRequest) {
		out.println("InjectionRequest: "
				+ pRequest);
		return null;
	}

	@Override
	public Object visit(StaticInjectionRequest pRequest) {
		out.println("StaticInjectionRequest: "
				+ pRequest);
		return null;
	}

	@Override
	public Object visit(ProviderLookup pLookup) {
		out.println("ProviderLookup: "
				+ pLookup);
		return null;
	}

	@Override
	public Object visit(MembersInjectorLookup pLookup) {
		out.println("MembersInjectorLookup: "
				+ pLookup);
		return null;
	}

	@Override
	public Object visit(Message pMessage) {
		out.println("Message: "
				+ pMessage);
		return null;
	}

	@Override
	public Object visit(PrivateElements pElements) {
		out.println("PrivateElements: "
				+ pElements);
		return null;
	}

	@Override
	public Object visit(TypeListenerBinding pBinding) {
		out.println("TypeListenerBinding: "
				+ pBinding);
		return null;
	}

	@Override
	public Object visit(ProvisionListenerBinding pBinding) {
		out.println("ProvisionListenerBinding: "
				+ pBinding);
		return null;
	}

	@Override
	public Object visit(RequireExplicitBindingsOption pOption) {
		out.println("RequireExplicitBindingsOption: "
				+ pOption);
		return null;
	}

	@Override
	public Object visit(DisableCircularProxiesOption pOption) {
		out.println("DisableCircularProxiesOption: "
				+ pOption);
		return null;
	}

	@Override
	public Object visit(RequireAtInjectOnConstructorsOption pOption) {
		out.println("RequireAtInjectOnConstructorsOption: "
				+ pOption);
		return null;
	}

	@Override
	public Object visit(RequireExactBindingAnnotationsOption pOption) {
		out.println("RequireExactBindingAnnotationsOption: "
				+ pOption);
		return null;
	}

	@Override
	public Object visit(ModuleAnnotatedMethodScannerBinding pBinding) {
		out.println("ModuleAnnotatedMethodsScannerBinding: "
				+ pBinding);
		return null;
	}

	/** Called to visualize the given modules binding registrations.
	 * @param pOut The output stream, to which registration events are being written.
	 * @param pModule The module, that is being visualized.
	 */
	public static void show(OutputStream pOut, com.google.inject.Module pModule) {
		final ElementVisitor<?> pev = new PrintingElementVisitor(pOut);
		for (Element element : Elements.getElements(pModule)) {
			element.acceptVisitor(pev);
		}
	}
}