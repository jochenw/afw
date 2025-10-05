package com.github.jochenw.afw.vdn;

import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;



/** A helper class for working with Vaadin components.
 */
public class Components {
	/** Private constructor, because this class contains
	 * only static utility methods.
	 */
	private Components() {
	}

	/** Displays a tree, which cobtains the given component as the root.
	 * and all it's subcomponents as the branches, and leafs.
	 * Internally, this will invoke {@link #showComponent(Component, Consumer, String)}
	 * for the root, the branches, and eventually the leaves.
	 * @param pComponent The root component.
	 * @param pWriter The writer, to which the tree is being written.
	 */
	public static void showTree(Component pComponent, Consumer<String> pWriter) {
		showComponent(pComponent, pWriter, "");
	}

	/** Displays a branch, or leaf in the Vaadin component tree.	 * 
	 * @param pComponent The branch, or leaf component, which is being
	 *   displayed.
	 * @param pWriter The writer, to which the branch, or leaf is being written.
	 * @param pIndent A string, which is being displayed as a prefix for
	 *   each line. The prefix is supposed to indicate the position of the
	 *   respective branch, or leave, in the tree, relative to the root.
	 */
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
