package org.workcraft.plugins.petri.tools;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.workcraft.Trace;

public class TracesClipboardOwner {
	public static TracePair loadFromClipboard() {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clip.getContents(null);
		boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		String str = "";

		if (hasTransferableText) {
			try {
				str = (String) contents.getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			} catch (IOException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}

		int i = 0;

		Trace trace = new Trace();
		Trace branchTrace = new Trace();
		int traceStep = 0;
		int branchStep = 0;

		for (String s : str.split("\n")) {
			if (i == 0) {
				trace.fromString(s);
			} else if (i == 1) {
				traceStep = Integer.valueOf(s);
			} else if (i == 2) {
				branchTrace = new Trace();
				branchTrace.fromString(s);
			} else if (i == 3) {
				branchStep = Integer.valueOf(s);
			}
			i++;
			if (i > 3)
				break;
		}
		
		return new TracePair(trace, traceStep, branchTrace, branchStep);
	}

	public static void saveToClipboard(TracePair tracePair) {
		final Trace trace = tracePair.trace;
		final Trace branchTrace = tracePair.branchTrace;
		final int traceStep = tracePair.traceStep;
		final int branchStep = tracePair.branchStep;

		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		String st = ((trace != null) ? trace.toString() : "") + "\n" + traceStep + "\n";
		String st2 = (branchTrace != null) ? branchTrace.toString() + "\n" + branchStep : "";
		StringSelection stringSelection = new StringSelection(st + st2);
		clip.setContents(stringSelection, null);
	}
}
