package org.workcraft.plugins.petri.tools

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

object TracesClipboardOwner {
  def loadFromClipboard : TracePair = {
    val clip = Toolkit.getDefaultToolkit.getSystemClipboard
    val contents = clip.getContents(null)
    val hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor)
    var str = ""

    if (hasTransferableText) {
      try {
	str = contents.getTransferData(DataFlavor.stringFlavor).asInstanceOf[String]
      } catch {
	case (ex : UnsupportedFlavorException) => {
	  System.out.println(ex)
	  ex.printStackTrace
	} 
	case (ex : IOException) => {
	  System.out.println(ex)
	  ex.printStackTrace()
	}
      }
    }

    str.split("\n").toList match {
      case List(t,ts,bt,bts) => {
	TracePair(TraceStep(Trace.fromString(t), Integer.valueOf(ts)), 
		     TraceStep(Trace.fromString(bt), Integer.valueOf(bts)))
      }
    }
  }

  def saveToClipboard(tracePair : TracePair) {
    val TracePair(TraceStep(trace,traceStep)
		  , TraceStep(branchTrace, branchStep)) = tracePair

    val clip = Toolkit.getDefaultToolkit.getSystemClipboard
    val st = (if (trace != null) trace.toString else "") + "\n" + traceStep + "\n"
    val st2 = if (branchTrace != null) branchTrace.toString + "\n" + branchStep else ""
    val stringSelection = new StringSelection(st + st2)
    clip.setContents(stringSelection, null)
  }
}
