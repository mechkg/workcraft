// Workcraft 2.0 Windows startup script.
// Enumerates jars in the "plugins" directory and adds them to the classpath
// before calling the main class.

var fso = new ActiveXObject("Scripting.FileSystemObject");
var folder = fso.GetFolder("plugins");
var files = new Enumerator(folder.Files);

var cp = "WorkcraftCore-2.0-SNAPSHOT.jar;";

for (var files = new Enumerator(folder.Files); !files.atEnd(); files.moveNext())
{
        var name = files.item().Name;
	if (name.match(".jar$")==".jar")
		cp += ".\\plugins\\" + files.item().Name + ";";
}

var command = "java -cp " + cp + " org.workcraft.Console";

for (var i = 0; i < WScript.Arguments.length; i++)
	command += " " + WScript.Arguments(i);

var shell = WScript.CreateObject("WScript.Shell");
shell.Run (command);
