package org.workcraft.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.ToolJob;
import org.workcraft.interop.ServiceNotAvailableException;
import org.workcraft.workspace.WorkspaceEntry;

public class Tools {
	public static ListMap<String, Pair<String, ToolJob>> getTools(WorkspaceEntry we, Framework framework) {
		ListMap<String, Pair<String, ToolJob>> toolSections = new ListMap<String, Pair<String, ToolJob>>();

		for (Tool tool : framework.getPluginManager().getPlugins(Tool.SERVICE_HANDLE)) {
			try {
				ToolJob toolJob = tool.applyTo(we);
				toolSections.put(tool.getSection(), new Pair <String,ToolJob> (tool.getDisplayName(), toolJob));
			} catch (ServiceNotAvailableException ex) {}

		}
		
		return toolSections;
	}
	
	public static List<String> getSections (ListMap<String, Pair<String, ToolJob>> tools) {
		LinkedList<String> list = new LinkedList<String>(tools.keySet());
		Collections.sort(list);
		return list;
	}
	
	public static List<Pair<String,ToolJob>> getSectionTools (String section, ListMap<String, Pair<String, ToolJob>> tools) {
		List<Pair<String,ToolJob>> sectionTools = new ArrayList<Pair<String, ToolJob>>(tools.get(section));

		Collections.sort(sectionTools, new Comparator<Pair<String,ToolJob>>() {
			@Override
			public int compare(Pair<String, ToolJob> o1,
					Pair<String, ToolJob> o2) {
				return (o1.getFirst().compareTo(o2.getFirst()));
			}
		});
		
		return sectionTools;
	}
}