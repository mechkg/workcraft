package org.workcraft.plugins.cpog;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import org.workcraft.dom.visual.Label;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;

public class FormulaRenderer {
	public static Font fancyFont;
	
	static {
		try {
			fancyFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static FormulaRenderingResult render(BooleanFormula formula) {
		return render(formula, fancyFont);
	}
	
	public static FormulaRenderingResult render(BooleanFormula formula, Font font) {
		return FormulaToGraphics.render(formula, Label.podgonFontRenderContext(), fancyFont);
	}
}
