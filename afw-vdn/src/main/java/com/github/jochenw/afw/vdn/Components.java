package com.github.jochenw.afw.vdn;

import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;


public class Components {

	public static void showTree(Component pComponent, Consumer<String> pWriter) {
		showComponent(pComponent, pWriter, "");
	}

	protected static void showComponent(Component pComponent, Consumer<String> pWriter, String pIndent) {
		final boolean visible = pComponent.isVisible();
		final String visibleInfo = visible ? "" : " (Invisible)";
		pWriter.accept(pIndent + pComponent.getClass().getName() + visibleInfo);
		final String indent = pIndent + "  ";
		if (pComponent instanceof HasOrderedComponents) {
			final HasOrderedComponents hc = (HasOrderedComponents) pComponent;
			final int count = hc.getComponentCount();
			if (count == 0) {
				pWriter.accept(indent + "(No children)");
			} else {
				for (int i = 0;  i < count;  i++) {
					showComponent(hc.getComponentAt(i), pWriter, indent);
				}
			}
		} else if (pComponent instanceof TabSheet) {
			final TabSheet ts = (TabSheet) pComponent;
			final int tabCount = ts.getTabCount();
			if (tabCount == 0) {
				pWriter.accept(indent + " (No tabs)");
			} else {
				for (int i = 0;  i < tabCount;  i++) {
					final Tab tab = ts.getTabAt(i);
					pWriter.accept(indent + ": Tab " + i);
					final String tabIndent = indent + "  ";
					showComponent(ts.getComponent(tab), pWriter, tabIndent);
				}
			}
		}
	}
}
