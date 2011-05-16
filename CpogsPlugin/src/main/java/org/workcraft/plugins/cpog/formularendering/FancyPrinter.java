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
package org.workcraft.plugins.cpog.formularendering;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.gui.propertyeditor.SettingsEditorDialog;
import org.workcraft.plugins.cpog.formularendering.FancyPrinter.PrinterSettings;
import org.workcraft.plugins.cpog.optimisation.BinaryBooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.*;
import org.workcraft.util.Function;
import org.workcraft.util.Function0;
import org.workcraft.util.Function2;
import org.workcraft.util.Maybe;
import org.workcraft.util.MaybeVisitor;
import org.workcraft.util.Nothing;
import org.workcraft.util.Pair;



public class FancyPrinter
{
	public class Void{ private Void(){} }
	
	public static Font defaultFont;
	public static Font defaultSubFont;
	
	static {
		try {
			defaultFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/default.pfb")).deriveFont(0.5f);
			
			Map<TextAttribute, Integer> attributes = new HashMap<TextAttribute, Integer>();
			attributes.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
			defaultSubFont = defaultFont.deriveFont(attributes);		
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public static FormulaRenderingResult print(String text, Font font, FontRenderContext fontRenderContext)
	{
		if (text.length() < 1) text = " ";
		
		Map<TextAttribute, Integer> attributes = new HashMap<TextAttribute, Integer>();
		attributes.put(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB);
		Font subfont = font.deriveFont(attributes);
		
		float fontSize = font.getSize2D();

		FormulaRenderingResult res = print(text.charAt(0), font, fontRenderContext);
		
		if (!font.canDisplay(text.charAt(0))) res = print(text.charAt(0), defaultFont.deriveFont(fontSize), fontRenderContext);

		
		int subIndex = text.lastIndexOf('_');
		if (subIndex < 0) subIndex = text.length();
		
		for(int i = 1; i < text.length(); i++)
		{
			if (i == subIndex) continue;
			
			if (i < subIndex)
			{
				if (font.canDisplay(text.charAt(i)))
					res.add(print(text.charAt(i), font, fontRenderContext));
				else
					res.add(print(text.charAt(i), defaultFont.deriveFont(fontSize), fontRenderContext));
			}
			else
			{
				if (subfont.canDisplay(text.charAt(i)))
					res.add(print(text.charAt(i), subfont, fontRenderContext));
				else
					res.add(print(text.charAt(i), defaultSubFont.deriveFont(fontSize), fontRenderContext));
			}
		}
	
		return res;
	}
	
	public static FormulaRenderingResult print(char c, Font font, FontRenderContext fontRenderContext)
	{
		FormulaRenderingResult result = new FormulaRenderingResult();
		
		GlyphVector glyphs;
		
		glyphs = font.createGlyphVector(fontRenderContext, "" + c);
		
		result.boundingBox = glyphs.getLogicalBounds();
		result.visualTop = glyphs.getVisualBounds().getMinY();
		
		result.glyphs = new ArrayList<GlyphVector>();
		result.glyphs.add(glyphs);
		
		result.glyphCoordinates = new ArrayList<Point2D>();
		result.glyphCoordinates.add(new Point2D.Double(0, 0));
		
		result.inversionLines = new ArrayList<Line2D>();
		
		return result;
	}

	
	public static class PrinterSuite
	{
		abstract static class List<A> implements Function0<Maybe<Pair<A, List<A>>>> {
			public static <A> List<A> empty() {
				return new List<A>(){
					@Override
					public Maybe<Pair<A, List<A>>> apply() {
						return Maybe.Util.nothing();
					}
				};
			}
			public static <A> List<A> cons(final A head, final List<A> tail) {
				return new List<A>() {
					@Override
					public Maybe<Pair<A, List<A>>> apply() {
						return Maybe.Util.just(Pair.of(head, tail));
					}
				};
			}
		}
		
		Function<List<List<Character>>, List<List<Character>>> a = new Function<List<List<Character>>, List<List<Character>>>(){
			@Override
			public List<List<Character>> apply(List<List<Character>> argument) {
				return List.cons(List.cons('a', List.<Character>empty()), argument);
			}
		};
		
		Function<List<List<Character>>, List<List<Character>>> b = new Function<List<List<Character>>, List<List<Character>>>(){
			@Override
			public List<List<Character>> apply(List<List<Character>> argument) {
				return List.cons(List.cons('b', List.<Character>empty()), argument);
			}
		};
		
		static <A> Function2<List<A>, List<A>, List<A>> concat() { 
			return new Function2<List<A>, List<A>, List<A>>(){
				@Override
				public List<A> apply(final List<A> argument1, final List<A> argument2) {
					return new List<A>(){

						@Override
						public Maybe<Pair<A, List<A>>> apply() {
							Maybe<Pair<A, List<A>>> res = argument1.apply();
							return res.accept(new MaybeVisitor<Pair<A, List<A>>, Maybe<Pair<A, List<A>>>>() {

								@Override
								public Maybe<Pair<A, List<A>>> visitJust(Pair<A, List<A>> just) {
									return cons(just.getFirst(), PrinterSuite.<A>concat().apply(just.getSecond(), argument2)).apply();
								}

								@Override
								public Maybe<Pair<A, List<A>>> visitNothing() {
									return argument2.apply();
								}
							});
						}
					};
				}
			};
		}
		
		static <A,B> List<B> zipWith(final Function2<A, A, B> func, final List<A> list1, List<A> list2) {
			return new List<B>(){
				@Override
				public Maybe<Pair<B, List<B>>> apply() {
					Maybe<Pair<A, List<A>>> l1 = list1.apply();
					Maybe<Pair<A, List<A>>> l2 = list1.apply();
					return Maybe.Util.bind(new Function<Pair<A, List<A>>, Maybe<Pair<B, List<B>>>>(){

						@Override
						public Maybe<Pair<B, List<B>>> apply(final Pair<A, List<A>> sl1) {
							return Maybe.Util.bind(new Function<Pair<A, List<A>>, Maybe<Pair<B, List<B>>>>(){

								@Override
								public Maybe<Pair<B, List<B>>> apply(Pair<A, List<A>> sl2) {
									return Maybe.Util.just(Pair.of(func.apply(sl1.getFirst(), sl2.getFirst()), zipWith(func, sl1.getSecond(), sl2.getSecond())));
								}
							});
						}
						
					});
				}
			};
		}
		
		Function2<List<List<Character>>, List<List<Character>>, List<List<Character>>> d = new Function2<List<List<Character>>, List<List<Character>>, List<List<Character>>>(){
			@Override
			public List<List<Character>> apply(List<List<Character>> argument1, List<List<Character>> argument2) {
				return new List<List<Character>>(){

					@Override
					public Maybe<Pair<List<Character>, List<List<Character>>>> apply() {
						// TODO Auto-generated method stub
						return null;
					}
					
				};
			}
		};
		
		public PrinterSuite()
		{
			iff = new IffPrinter();
			imply = new ImplyPrinter();
			or = new OrPrinter();
			xor = new XorPrinter();
			and = new AndPrinter();
			not = new NotPrinter(iff);
			constants = new ConstantPrinter();
			vars = new VariablePrinter();
			paren = new ParenthesesPrinter();
		}
		
		public void init(FontRenderContext fontRenderContext, Font font, boolean unicodeAllowed)
		{
			init(iff, imply, fontRenderContext, font, unicodeAllowed);
			init(imply, or, fontRenderContext, font, unicodeAllowed);
			init(or, xor, fontRenderContext, font, unicodeAllowed);
			init(xor, and, fontRenderContext, font, unicodeAllowed);
			init(and, not, fontRenderContext, font, unicodeAllowed);
			init(not, vars, fontRenderContext, font, unicodeAllowed);
			init(vars, constants, fontRenderContext, font, unicodeAllowed);
			init(constants, paren, fontRenderContext, font, unicodeAllowed);
			init(paren, iff, fontRenderContext, font, unicodeAllowed);
		}
		
		public StringBuilder builder;
		public IffPrinter iff;
		public ImplyPrinter imply;
		public OrPrinter or;
		public XorPrinter xor;
		public AndPrinter and;
		public NotPrinter not;
		public ConstantPrinter constants;
		public VariablePrinter vars;
		public ParenthesesPrinter paren;
	}
	
	class PrinterSettings<Var> {
		public final DelegatingPrinter<Var> next;
		public final FontRenderContext fontRenderContext;
		public final Font font;
		
		public PrinterSettings(DelegatingPrinter<Var> next,
				FontRenderContext fontRenderContext, Font font) {
			super();
			this.next = next;
			this.fontRenderContext = fontRenderContext;
			this.font = font;
		}
	}
	
	public static class DelegatingPrinter<Var> extends BooleanVisitor<Var, FormulaRenderingResult>
	{
		public final PrinterSettings<Var> settings;

		public DelegatingPrinter(PrinterSettings<Var> settings) {
			this.settings = settings;
		}

		public final boolean unicodeAllowed = false;

		public FormulaRenderingResult print(String text)
		{
			return FancyPrinter.print(text, settings.font, settings.fontRenderContext);
		}
		
		protected FormulaRenderingResult visitBinary(DelegatingPrinter<Var> printer, String opSymbol, BinaryBooleanFormula<Var> node)
		{
			FormulaRenderingResult res = node.getX().accept(printer);

			res.add(print(opSymbol));
			
			res.add(node.getY().accept(printer));
						
			return res;
		}
		
		@Override
		public FormulaRenderingResult visit(And<Var> node) {
			return settings.next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Iff<Var> node) {
			return settings.next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Zero<Var> node) {
			return settings.next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(One<Var> node) {
			return settings.next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Not<Var> node) {
			return settings.next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Imply<Var> node) {
			return settings.next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Variable<Var> node) {
			return settings.next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Or<Var> node) {
			return settings.next.visit(node);
		}

		@Override
		public FormulaRenderingResult visit(Xor<Var> node) {
			return settings.next.visit(node);
		}
	}
	
	public static class IffPrinter<Var> extends DelegatingPrinter<Var>
	{
		public IffPrinter(PrinterSettings<Var> settings) {
			super(settings);
		}
		
		@Override
		public FormulaRenderingResult visit(Iff<Var> node) {
			return visitBinary(this, " = ", node);
		}
	}
	
	public static class ImplyPrinter<Var> extends DelegatingPrinter<Var>
	{
		public ImplyPrinter(PrinterSettings<Var> settings) {
			super(settings);
		}
		
		@Override
		public FormulaRenderingResult visit(Imply<Var> node) {
			return visitBinary(settings.next, unicodeAllowed ? " \u21d2 " : " => ", node);
		}
	}
	
	public static class OrPrinter<Var> extends DelegatingPrinter<Var>
	{
		public OrPrinter(PrinterSettings<Var> settings) {
			super(settings);
		}
		
		@Override
		public FormulaRenderingResult visit(Or<Var> node) {
			return visitBinary(this, " + ", node);
		}
	}
	
	public static class XorPrinter<Var> extends DelegatingPrinter<Var>
	{
		@Override
		public FormulaRenderingResult visit(Xor<Var> node) {
			return visitBinary(this, unicodeAllowed ? " \u2295 " : " ^ ", node);
		}
	}
	
	public static class AndPrinter<Var> extends DelegatingPrinter<Var>
	{
		@Override
		public FormulaRenderingResult visit(And<Var> node) {
			return visitBinary(this, unicodeAllowed ? "\u00b7" : "*", node);
		}
	}
	
	public static class NotPrinter<Var> extends DelegatingPrinter<Var>
	{		
		private final IffPrinter<Var> iff;

		public NotPrinter(IffPrinter<Var> iff)
		{
			this.iff = iff;			
		}

		@Override
		public FormulaRenderingResult visit(Not<Var> node)
		{
			FormulaRenderingResult res = node.getX().accept(iff);
			
			res.visualTop -= settings.font.getSize2D() / 8.0;
			
			res.inversionLines.add(new Line2D.Double(
				res.boundingBox.getMinX(), res.visualTop,
				res.boundingBox.getMaxX(), res.visualTop));
			
			
			res.boundingBox.add(new Point2D.Double(res.boundingBox.getMaxX(), res.boundingBox.getMinY() - settings.font.getSize2D() / 8.0));
			
			return res;  
		}
	}
	
	public static class ConstantPrinter extends DelegatingPrinter
	{
		@Override
		public FormulaRenderingResult visit(One one)
		{
			return print("1");
		}
		@Override
		public FormulaRenderingResult visit(Zero zero) {
			return print("0");
		}
	}
	
	public static class VariablePrinter extends DelegatingPrinter
	{
		Map<String, BooleanVariable> varMap = new HashMap<String, BooleanVariable>();
		@Override
		public FormulaRenderingResult visit(BooleanVariable var) {
			String label = var.getLabel();
			BooleanVariable nameHolder = varMap.get(label);
			if(nameHolder == null)
				varMap.put(label, var);
			else
				if(nameHolder != var)
					throw new RuntimeException("name conflict! duplicate name " + label);
			
			return print(label);
		}
	}
	
	public static class ParenthesesPrinter extends DelegatingPrinter
	{
		@Override public FormulaRenderingResult visit(Zero node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(One node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(BooleanVariable node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(And node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(Or node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(Xor node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(Iff node) { return enclose(node); }
		@Override public FormulaRenderingResult visit(Imply node) { return enclose(node); }
		FormulaRenderingResult enclose(BooleanFormula node)
		{
			FormulaRenderingResult res = print("(");
			res.add(node.accept(next));
			res.add(print(")"));
			return res;
		}
	}

	public static FormulaRenderingResult render(BooleanFormula formula, FontRenderContext fontRenderContext, Font font)
	{
		return render(formula, fontRenderContext, font, true);
	}	

	public static FormulaRenderingResult render(BooleanFormula formula, FontRenderContext fontRenderContext, Font font, boolean unicodeAllowed)
	{
		PrinterSuite ps = new PrinterSuite();
		ps.init(fontRenderContext, font, unicodeAllowed);
		
		return formula.accept(ps.iff);		
	}	
}
