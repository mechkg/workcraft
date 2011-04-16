/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.util.Action;
import org.workcraft.util.Function;
import org.workcraft.util.Nothing;

public interface EditableProperty {
	class Util {
		public static <T> EditableProperty create(final String propertyName, final GenericEditorProvider<T> editor, final RendererProvider<T> renderer, final ModifiableExpression<T> property) {
			
			return new EditableProperty(){
				@Override
				public ReactiveComponent renderer(Expression<Boolean> isSelected, Expression<Boolean> hasFocus) {
					final JPanel panel = new JPanel();
					return new ReactiveComponent() {
						@Override
						public Expression<? extends Nothing> updateExpression() {
							return Expressions.fmap(new Function<T, Nothing>(){
								@Override
								public Nothing apply(T value) {
									panel.removeAll();
									panel.setLayout(new BorderLayout());
									panel.add(renderer.createRenderer(value), BorderLayout.CENTER);
									return Nothing.VALUE;
								}
							}, property);
						}
						@Override
						public Component component() {
							return panel;
						}
					};
				}

				@Override
				public Expression<? extends EditorProvider> editorMaker() {
					return Expressions.fmap(new Function<T, EditorProvider>(){

						@Override
						public EditorProvider apply(final T value) {
							return new EditorProvider() {
								
								@Override
								public SimpleCellEditor getEditor(final Action close) {
									return new SimpleCellEditor() {

										GenericCellEditor<T> ge = editor.createEditor(value, new Action(){

											@Override
											public void run() {
												commit();
												close.run();
											}
										}, close);
										
										@Override
										public Component getComponent() {
											return ge.component();
										}
										
										@Override
										public void commit() {
											property.setValue(ge.getValue());
										}
									};
								}
							};
						}
						
					}, property);
				}

				@Override
				public String name() {
					return propertyName;
				}
			};
		}
	}
	public String name();
	public ReactiveComponent renderer(Expression<Boolean> isSelected, Expression<Boolean> hasFocus);
	public Expression<? extends EditorProvider> editorMaker();
}
